#####
# AUTORES: Beatriz Perez, Ana Roig
#     NIA: 683546,        686329
# Fichero:servidor_gv.exs
#  Tiempo: 0.5h
# Descripcion: Clase correspondiente al cliente externo,encargado de pedir operaciones
#####
Code.require_file("#{__DIR__}/cliente_gv.exs")

defmodule ClienteSA do
    
    @doc """
        Poner en marcha un nodo cliente del servicio de almacenamiento
    """
    @spec start(String.t, String.t, node) :: node
    def start(host, nombre_nodo, nodo_servidor_gv) do

        nodo = NodoRemoto.start(host, nombre_nodo, __ENV__.file,__MODULE__)

        Node.spawn(nodo, __MODULE__, :init, [nodo_servidor_gv])

        nodo    
    end


    @doc """
       Obtener el valor en curso de la clave
       - Devuelve cadena vacia de caracteres ("") si no existe la clave
       - Seguir intentandolo  en el resto de situaciones de fallo o error
       La especificación del interfaz de la función es la siguiente :
                            (se puede utilizar también para "dialyzer")
    """
    @spec lee( node(), String.t ) :: String.t
    def lee(nodo_cliente, clave) do
        send({:cliente_sa, nodo_cliente}, {:lee, clave, self()})

        receive do
            {:resultado, valor} -> valor

            _otro -> exit("ERROR de programa : funcion lee en modulo CLienteSA")
        end
    end


    @doc """
       Escribir un valor para una clave
       - Seguir intentandolo hasta que se tenga exito
       - Devuelve valor anterior si hash y nuevo sino
       La especificación del interfaz de la función es la siguiente :
                            (se puede utilizar también para "dialyzer")
    """
    @spec escribe_generico( node(), String.t, String.t, boolean, boolean ) :: String.t
    def escribe_generico(nodo_cliente, clave, nuevo_valor, espera, con_hash) do
        send({:cliente_sa, nodo_cliente}, {:escribe_generico, 
                                        {clave, nuevo_valor, espera, con_hash}, self()})

        receive do
            {:resultado, valor} -> valor

            otro ->
                :io.format "otro en ClienteSA.escribe_generico : ~p~n", [otro]

                Process.sleep 100

                exit("ERROR: funcion escribe_generico en modulo CLienteSA")
        end       
    end
     

    @doc """
       - Devuelve nuevo valor escrito
    """
    @spec escribe( node(), String.t, String.t, boolean ) :: String.t
    def escribe(nodo_cliente, clave, nuevo_valor, espera) do
        escribe_generico(nodo_cliente, clave, nuevo_valor, espera, false)
    end

    @doc """
       - Devuelve valor anterior
    """
    @spec escribe_hash( node(), String.t, String.t, boolean ) :: String.t
    def escribe_hash(nodo_cliente, clave, nuevo_valor, espera) do
        escribe_generico(nodo_cliente, clave, nuevo_valor, espera, true)
    end
    

    #------------------- Funciones privadas ---------------------------------

    def init(nodo_servidor_gv) do
        Process.register(self(), :cliente_sa)

        bucle_recepcion(nodo_servidor_gv)
    end


    defp bucle_recepcion(servidor_gv) do
        receive do
            {op, param, pid} when (op == :lee) or (op == :escribe_generico) ->
                resultado = realizar_operacion(op, param, servidor_gv, true)
                send(pid, {:resultado, resultado})
                bucle_recepcion(servidor_gv)

            _otro -> exit("ERROR: mensaje erroneo en ClienteSA.bucle_recepcion")
        end   
    end


    defp realizar_operacion(op, param, servidor_gv, primeraVez) do
        # Obtener el primario del servicio de almacenamiento
        p = ClienteGV.primario(servidor_gv)

        case p do
            :undefined ->  # esperamos un rato si aparece primario
                Process.sleep(ServidorGV.intervalo_latido())
                realizar_operacion(op, param, servidor_gv, true)

            nodo_primario ->   # enviar operación a ejecutar a primario

                send({:servidor_sa, nodo_primario}, {op, param, Node.self(), primeraVez})

                # recuperar resultado
                receive do
                    {:resultado, :no_soy_primario_valido} ->
                        IO.puts("No soy primario valido")
                        realizar_operacion(op, param, servidor_gv, true)

                    {:resultado, valor} ->
                        IO.puts("Cliente recibe valor #{valor}")
                        valor

                # Sin resultado en tiempo establecido ?
                # -> se vuelve a pedir operacion al primario en curso
                after ServidorGV.intervalo_latido() ->
                    IO.puts("Reintento")
                    #cambiar espera a false
                    realizar_operacion(op, put_elem(param, 2, false), servidor_gv, false)
                end
        end
    end

 
end
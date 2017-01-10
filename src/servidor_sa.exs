
defmodule ServidorSA do
    
                
    defstruct  bbdd: %{}, estado: %{}


    @intervalo_latido 50


    @doc """
        Obtener el hash de un string Elixir
            - Necesario pasar, previamente,  a formato string Erlang
         - Devuelve entero
    """
    def hash(string_concatenado) do
        String.to_charlist(string_concatenado) |> :erlang.phash2
    end


    @doc """
        Poner en marcha un servidor de almacenamiento
    """
    @spec start(String.t, String.t, node) :: node
    def start(host, nombre_nodo, nodo_servidor_gv) do
        nodo = NodoRemoto.start(host, nombre_nodo, __ENV__.file, __MODULE__)
        
        Node.spawn(nodo, __MODULE__, :init_sa, [nodo_servidor_gv])

        nodo
    end


    #------------------- Funciones privadas -----------------------------

    def init_sa(nodo_servidor_gv) do
        Process.register(self(), :servidor_sa)
        nodo = NodoRemoto.start("127.0.0.1", procesoLatidos, __ENV__.file, __MODULE__)
        Node.spawn(nodo, __MODULE__, :enviarLatido, [nodo_servidor_gv, 0])
        Process.register(self(), :cliente_gv)

 



    #------------- VUESTRO CODIGO DE INICIALIZACION AQUI..........



         # Poner estado inicial
        bucle_recepcion_principal(ServidorGV.vista_inicial(), nodo_servidor_gv, %ServidorSA{})
    end


    def enviarLatido(nodo_servidor_gv, vista) do
      send(self(), :enviarLatido)

      Process.sleep(50)
      enviarLatido(nodo_servidor_gv, vistaTentativa)
    end

    defp bucle_recepcion_principal(vista, nodo_servidor_gv, almacen) do
        {nuevaVista, nodo_servidor_gv, almacen} = receive do

            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {op, param, nodo_origen}  ->
                #hablar con los otros pa ver si estan vivos o hay fallo red?? hablar con el GV es suficiente?
                {vistaValida, coincide} == ClienteGV.obten_vista(nodo_servidor_gv)
                if(vistaValida.primario == Node.self() && coincide) do
                    propagacion()
                    cond do
                        op == :lee ->
                        op == :escribe_generico ->
                    end
                else
                    send({:cliente_sa, nodo_origen}, {:resultado, :no_soy_primario_valido})
                end


                ""
            {:transferencia, almacenRecibido} ->
                respuesta = {vista, nodo_servidor_gv, almacenRecibido} #Proceso de transferencia
                send({:servidor_sa, vista.primario}, {:okTransferencia})
                respuesta
            {:enviarLatido} ->
                vistaTentativa = ClienteGV.latido(nodo_servidor_gv, vista.num_vista)

                if(vistaTentativa.primario == Node.self() || vistaTentativa.copia == Node.self()) do #marginacion a los secundarios
                    comprobarTransferencia(vista, vistaTentativa, nodo_servidor_gv, almacen)
                    #no hay else -> si no hay copia,no se validara la vista, y si es la copia, ya esta como primario en la tentativa
                end


                # ----------------- vuestro códio


        # --------------- OTROS MENSAJES QE NECESITEIS
        end
        bucle_recepcion_principal(nuevaVista, nodo_servidor_gv)
        end


        defp comprobarTransferencia(vista, vistaTentativa, nodo_servidor_gv, almacen) do
          {vistaValida, ok} = ClienteGV.obten_vista(nodo_servidor_gv)
          if(vistaValida != vistaTentativa && vistaTentativa.copia != :undefined           #no coinciden la valida y la tentativa ->hay caida, y soy primario -> copia
            && vistaTentativa.primario == Node.self()) do
              if(hacerTransferencia(almacen, vistaTentativa.copia)) do #transferir a la nueva copia
                  vistaTentativa = ClienteGV.latido(nodo_servidor_gv, vistaTentativa.num_vista)
                  {vistaTentativa, nodo_servidor_gv, almacen}
              else
                  ClienteGV.latido(nodo_servidor_gv, vista.num_vista)
                  {vista, nodo_servidor_gv, almacen}
              end
          end
        end


        defp hacerTransferencia(almacen, copia) do
            send({:servidor_sa, copia}, {:transferencia, almacen})
            receive do
              {:okTransferencia} -> true
            after 1000 -> false     #la copia no ha confirmado su transferencia! FALLO
            end
        end
    end
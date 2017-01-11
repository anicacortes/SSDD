
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
        spawn(__MODULE__, :enviarLatido, [self()])
    #------------- VUESTRO CODIGO DE INICIALIZACION AQUI..........
        IO.puts(" #{nodo_servidor_gv}, #{%ServidorSA{}}")
         # Poner estado inicial
        bucle_recepcion_principal(ServidorGV.vista_inicial(), nodo_servidor_gv, %ServidorSA{})
    end


    def enviarLatido(pidServidor) do
      send(pidServidor, :enviarLatido)

      Process.sleep(50)
      enviarLatido(pidServidor)
    end

    defp bucle_recepcion_principal(vista, nodo_servidor_gv, almacen) do
        {nuevaVista, nodo_servidor_gv, almacen} = receive do

            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {op, param, nodo_origen, primeraVez}  ->
                #hablar con los otros pa ver si estan vivos o hay fallo red?? hablar con el GV es suficiente?
                {vistaValida, coincide} = ClienteGV.obten_vista(nodo_servidor_gv)
                if(vistaValida.primario == Node.self() && coincide) do
                    okProp = propagacion(vistaValida.copia, op, param, nodo_origen, primeraVez)
                    if (okProp) do
                       {almacen,valor} = guardarEstado(op, param, nodo_origen, primeraVez, almacen)
                       send({:cliente_sa, nodo_origen}, {:respuesta, valor})
                    end
                else
                    send({:cliente_sa, nodo_origen}, {:resultado, :no_soy_primario_valido})
                end
                {vista, nodo_servidor_gv, almacen}

            #La copia recibe el almacen de datos y lo guarda
            {:transferencia, almacenRecibido} ->
                respuesta = {vista, nodo_servidor_gv, almacenRecibido} #Proceso de transferencia
                send({:servidor_sa, vista.primario}, {:okTransferencia})
                respuesta

            #La copia guarda los nuevos datos que se han propagado
            {:propagacion, op, param, nodo_origen, primeraVez} ->
                 {almacen,_} = guardarEstado(op, param, nodo_origen, primeraVez, almacen)
                 send({:servidor_sa, vista.primario}, {:okPropagacion})

            #Se realiza la gestion del latido
            {:enviarLatido} ->
                vistaTentativa = ClienteGV.latido(nodo_servidor_gv, vista.num_vista)

                if(vistaTentativa.primario == Node.self() || vistaTentativa.copia == Node.self()) do #marginacion a los secundarios
                    comprobarTransferencia(vista, vistaTentativa, nodo_servidor_gv, almacen)
                    #no hay else -> si no hay copia,no se validara la vista, y si es la copia, ya esta como primario en la tentativa
                end


                # ----------------- vuestro códio


        # --------------- OTROS MENSAJES QE NECESITEIS
        end
        bucle_recepcion_principal(nuevaVista, nodo_servidor_gv,almacen)
        end

        #Propagar a la copia para q lo almacene en bd y guarde estado
        defp propagacion(copia, op, param, nodo_origen, primeraVez) do
            send({:servidor_sa, copia}, {:propagacion, op, param, nodo_origen, primeraVez})
            receive do
              {:okPropagacion} -> true
            after @intervalo_latido -> false    #la copia no ha guardado la info
            end
        end

        #guarda estado/bd cuando se realiza una lectura por primera vez
        defp guardarEstado(:lee, param, nodo_origen, true, almacen) do
             estadoNuevo = Map.put(almacen.estado, nodo_origen, Tuple.elem(param,1))
             {%{almacen | estado: estadoNuevo}, Map.get(almacen.bbdd, param)}
        end

        #guarda estado/bd cuando se realiza una ecritura por primera vez
        defp guardarEstado(:escribe_generico, param, nodo_origen, true, almacen) do
            bbddNuevo = Map.put(almacen.bbdd, Tuple.elem(param,0), Tuple.elem(param,1))
            estadoNuevo = Map.put(almacen.estado, nodo_origen, Tuple.elem(param,1))
            {%{almacen | bbdd: bbddNuevo, estado: estadoNuevo}, Tuple.elem(param,1)}
        end

        #devuelve valor del utlimo estado cuando se realiza una lectura/ecritura y no es la primera vez
        defp guardarEstado(op, param, nodo_origen, false, almacen) do
             {almacen, Map.get(almacen.estado, nodo_origen)}
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
            after @intervalo_latido -> false     #la copia no ha confirmado su transferencia! FALLO
            end
        end
    end
#####
# AUTORES: Beatriz Perez, Ana Roig
#     NIA: 683546,        686329
# Fichero:servidor_gv.exs
#  Tiempo: 6h
# Descripcion: Clase correspondiente al servidor de almacenamiento
#####
Code.require_file("#{__DIR__}/cliente_gv.exs")

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
         # Poner estado inicial
        bucle_recepcion_principal(ServidorGV.vista_inicial(), nodo_servidor_gv, %ServidorSA{})
    end


    def enviarLatido(pidServidor) do
      send(pidServidor, {:enviarLatido})
      Process.sleep(50)
      enviarLatido(pidServidor)
    end

    defp bucle_recepcion_principal(vista, nodo_servidor_gv, almacen) do
        {nuevaVista, nodo_servidor_gv, almacen} = receive do

            #La copia guarda los nuevos datos que se han propagado
            {:propagacion, op, param, nodo_origen, primeraVez} ->
                 IO.puts("Propagacion operacion..")
                 {almacen,_} = guardarEstado(op, param, nodo_origen, primeraVez, almacen)
                 send({:servidor_sa, vista.primario}, {:okPropagacion})
                 {vista, nodo_servidor_gv, almacen}

            #La copia recibe el almacen de datos y lo guarda
            {:transferencia, almacenRecibido} ->
                respuesta = {vista, nodo_servidor_gv, almacenRecibido}
                send({:servidor_sa, vista.primario}, {:okTransferencia})
                respuesta

            #Se realiza la gestion del latido
            {:enviarLatido} ->
                numVista = cond do
                  vista.primario == Node.self() and vista.copia == :undefined -> -1
                  true -> vista.num_vista
                end
                {vistaTentativa, _} = ClienteGV.latido(nodo_servidor_gv, numVista)

                if (vistaTentativa.primario == Node.self()) do
                    comprobarTransferencia(vista, vistaTentativa, nodo_servidor_gv, almacen)
                end
                {vistaTentativa, nodo_servidor_gv, almacen}

            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {op, param, nodo_origen, primeraVez}  ->
                IO.puts("Solicitud de #{op} de #{nodo_origen}")
                {vistaValida, coincide} = ClienteGV.obten_vista(nodo_servidor_gv)
                cond do

                    #Si es pa primera vez, o es reintento pero el valor a devolver
                    #no ha sido almacenado -> propagar
                    vistaValida.primario == Node.self() && coincide && (primeraVez or
                       (op == :lee && Map.get(almacen.estado,nodo_origen)
                         != Map.get(almacen.bbdd,param))
                       or (op == :escribe_generico && Map.get(almacen.estado,nodo_origen)
                         != Map.get(almacen.bbdd,elem(param,0)))) ->
                        okProp = propagacion(vistaValida.copia, op, param,
                          nodo_origen, primeraVez)
                        if (okProp) do
                            IO.puts("Propagacion correcta")
                           {almacen,valor} = guardarEstado(op, param, nodo_origen,
                             primeraVez, almacen)
                           IO.puts("Guardado estado y bbdd: #{nodo_origen} - #{valor}")

                           #espera para no contestar a cliente --> simula fallo
                           if op == :escribe_generico && elem(param, 2) do
                               Process.sleep(50)
                               end
                           send({:cliente_sa, nodo_origen}, {:resultado, valor})
                        end

                    #Si es un reintento -> se devuelve lo d la ultima operacion del estado
                    vistaValida.primario == Node.self() and coincide and !primeraVez ->
                         {almacen,valor} = guardarEstado(op, param, nodo_origen,
                            primeraVez, almacen)
                          IO.puts("Devolviendo sin guardar: #{nodo_origen} - #{valor}")
                         send({:cliente_sa, nodo_origen}, {:resultado, valor})
                    true ->
                       send({:cliente_sa, nodo_origen}, {:resultado, :no_soy_primario_valido})
                end
                {vista, nodo_servidor_gv, almacen}
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
             estadoNuevo = Map.put(almacen.estado, nodo_origen, Map.get(almacen.bbdd, param))
             {%{almacen | estado: estadoNuevo}, Map.get(almacen.bbdd, param)}
        end

        #guarda estado/bd cuando se realiza una ecritura por primera vez
        defp guardarEstado(:escribe_generico, param, nodo_origen, true, almacen) do
            bbddNuevo = Map.put(almacen.bbdd, elem(param,0), elem(param,1))
            estadoNuevo = Map.put(almacen.estado, nodo_origen, elem(param,1))
            {%{almacen | bbdd: bbddNuevo, estado: estadoNuevo}, elem(param,1)}
        end

        #devuelve valor del utlimo estado cuando se realiza una lectura/ecritura y no es la primera vez
        defp guardarEstado(_, _, nodo_origen, false, almacen) do
             {almacen, Map.get(almacen.estado, nodo_origen)}
        end

        #Comprueba si hay que realizar transferencia por caso de fallo
        defp comprobarTransferencia(vista, vistaTentativa, nodo_servidor_gv, almacen) do
          {vistaValida, _} = ClienteGV.obten_vista(nodo_servidor_gv)
          #no coinciden la valida y la tentativa ->hay caida, y soy primario -> copia
          if(vistaValida != vistaTentativa && vistaTentativa.copia != :undefined) do
              IO.puts("Transferencia almacen...")
              if(hacerTransferencia(almacen, vistaTentativa.copia)) do
                  {vistaTentativa,_} = ClienteGV.latido(nodo_servidor_gv,
                     vistaTentativa.num_vista)
                  {vistaTentativa, nodo_servidor_gv, almacen}
              else
                  {vista, nodo_servidor_gv, almacen}
              end
          end
        end


        defp hacerTransferencia(almacen, copia) do
            send({:servidor_sa, copia}, {:transferencia, almacen})
            receive do
              {:okTransferencia} ->
               IO.puts("OkTransferencia")
               true
            after @intervalo_latido -> false #la copia no ha confirmado su transferencia! FALLO
            end
        end
    end
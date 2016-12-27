defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    #num_vista,idPrimario,idCopia
    defstruct  vistaValida: %{num_vista: 0, primario: :undefined, copia: :undefined},
            vistaTentativa: %{num_vista: 0, primario: :undefined, copia: :undefined},
            #nodosEspera: [],
            situacionServidores: %{:latidosGV => 0} #map estado servidores
            #"latidosP" => 0, "latidosC" => 0
    @type t_vista :: %{num_vista: integer, primario: node, copia: node}

    @tiempo_espera_carga_remota 1000

    @periodo_latido 50

    @latidos_fallidos 4 #4 o 5 latidos fallidos?


   @doc """
        Generar una estructura de datos vista inicial
    """
    def vista_inicial() do
        #%ServidorGV{vistaValida: {0,:undefined,:undefined}, vistaTentativa: {0,:undefined,:undefined}}
        %ServidorGV{}.vistaTentativa
    end

    @doc """
        Poner en marcha el servidor para gestión de vistas
    """
    @spec start(String.t, String.t) :: atom
    def start(host, nombre_nodo) do
        nodo = NodoRemoto.start(host, nombre_nodo,__ENV__.file,
                                __MODULE__)

        Node.spawn(nodo, __MODULE__, :init_sv, [])

        nodo
    end


    #------------------- Funciones privadas

    # Estas 2 primeras deben ser defs para llamadas tipo (MODULE, funcion,[])
    def init_sv() do
        Process.register(self(), :servidor_gv)

        spawn(__MODULE__, :init_monitor, [self()]) # otro proceso concurrente

        #### VUESTRO CODIGO DE INICIALIZACION
        #vista_inicial() #Llamar al metodo de inicio

        bucle_recepcion(%ServidorGV{})
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion(vista) do
    #esa vista devuelta es la actualizada en el metodo?
        nueva_vista = receive do
           {:latido, nodo_origen, n_vista} ->
                IO.puts("Recibo latido...")
                IO.puts("BUcLE RECEPCION: num_vista #{vista.vistaTentativa.num_vista} primario #{vista.vistaTentativa.primario} copia #{vista.vistaTentativa.copia}")
                #nueva_vista = procesa_latido(nodo_origen, n_vista, vista)
                #valor =  Map.get(vista.situacionServidores, :latidosGV) + 1
                #Meter un nodo en la lista que gestiona los latidos (tambien se meten lo q quedan en espera)
                nueva_situacionServidores = Map.put(vista.situacionServidores, nodo_origen, Map.get(vista.situacionServidores, :latidosGV) + 1)
                #nueva_situacionServidores = %{vista.situacionServidores | nodo_origen => vista.situacionServidores[:latidosGV]+1}
                nuevaVista = %{vista | situacionServidores: nueva_situacionServidores}
                nuevaV = procesa_latido(nodo_origen, n_vista, nuevaVista)
                IO.puts("DESPUES PROC_LATIDO: vistaTentativa: #{Map.get(nuevaV.vistaTentativa, :num_vista)} primario #{Map.get(nuevaV.vistaTentativa, :primario)}")
                send({:cliente_gv, nodo_origen}, {:vista_tentativa, nuevaV.vistaTentativa, true})
                nuevaV
#                cond do
#                  nodo_origen == nueva_vista.vistaTentativa.primario ->
#                    valor = Map.get(nueva_vista.situacionServidores, "latidosP")
#                    vivos = Map.put(nueva_vista.situacionServidores, "latidosP", valor+1)
#                  nueva_vista = %{nueva_vista | situacionServidores: vivos}
#                end
#                if(nodo_origen == nueva_vista.vistaTentativa.primario) do
#                    valor = Map.get(nueva_vista.situacionServidores, "latidosP")
#                    vivos = Map.put(nueva_vista.situacionServidores, "latidosP", valor+1)
#                    nueva_vista = %{nueva_vista | situacionServidores: vivos}
#                    #%{map | latidosP: 1} #1 o mas, como sumarlo?
#                else if(nodo_origen == nueva_vista.vistaTentativa.copia)
#                    valor = Map.get(nueva_vista.situacionServidores, "latidosC")
#                    vivos = Map.put(nueva_vista.situacionServidores, "latidosC", valor+1)
#                    #%{map | latidosC: 1} #1 o mas
#                    nueva_vista = %{nueva_vista | situacionServidores: vivos}
#
#                    //tentativa = %{nueva_vista.v_tentativa | primario: vivos}
#                    // nueva_vista = %{nueva_vista | v_tentativa: tentativa}
#                end
#                send(nodo_origen,{:vista_tentativa, nueva_vista.vistaTentativa,
#                nueva_vista.vistaValida.num_vista==n_vista})
#                nueva_vista #para devolverlo

           {:obten_vista, pid} ->
                ### VUESTRO CODIGO
                if vista.vistaValida.primario == :undefined do
                    IO.inspect "primario indefinido"
                    send(pid, {:vista_valida, vista.vistaValida, false})
                else
                    send(pid, {:vista_valida, vista.vistaValida,
                    vista.vistaValida.num_vista==vista.vistaTentativa.num_vista})
                end
                vista
           :procesa_situacion_servidores ->
                ### VUESTRO CODIGO
                procesar_situacion_servidores(vista)
        end
        bucle_recepcion(nueva_vista) #ponerlo en el latido y obten_vista para q pueda parar el procesa?
    end

    defp procesa_latido(nodo_origen, n_vista, vista) do
        #Asignar el numero de latidos + 1 del GV en el nodo q envia latido -> evitar desfases (justo antes del 4 latido sin enviar
        #envia uno, contar desde entonces el tiempo! no desde el servidor)
        cond do
          n_vista==0 ->
            cond do
              vista.vistaTentativa.primario == :undefined ->
                    IO.puts("Antes num_vista: #{Map.get(vista.vistaTentativa, :num_vista)} primario #{Map.get(vista.vistaTentativa, :primario)}")
                    nuevaVistaT = %{vista.vistaTentativa | num_vista: Map.get(vista.vistaTentativa, :num_vista) + 1, primario: nodo_origen}
                    vistaN = %{vista | vistaTentativa: nuevaVistaT}
                    IO.puts("Despues num_vista: #{Map.get(vistaN.vistaTentativa, :num_vista)} primario #{Map.get(vistaN.vistaTentativa, :primario)}")
                    vistaN
              vista.vistaTentativa.copia == :undefined ->
                    #La vista se valida porque existe primario y copia
                    IO.puts("Antes num_vista: #{Map.get(vista.vistaTentativa, :num_vista)} copia #{Map.get(vista.vistaTentativa, :copia)}")
                    nuevaVistaT = %{vista.vistaTentativa | num_vista: Map.get(vista.vistaTentativa, :num_vista) + 1, copia: nodo_origen}
                    vistaN = %{vista | vistaTentativa: nuevaVistaT}
                    vistaN = %{vistaN | vistaValida: nuevaVistaT}
                    IO.puts("Despues num_vista: #{Map.get(vistaN.vistaTentativa, :num_vista)} copia #{Map.get(vistaN.vistaTentativa, :copia)}")
                    vistaN
              true ->
                    vista
            end
          n_vista != -1 ->
            cond do
              #validar la vista despues de hacer la copia de los datos
              n_vista != vista.vistaValida.num_vista and vista.vistaTentativa.primario == nodo_origen -> #esto lo hace solo la copia
                    IO.puts("VALIDAR vista tras fallo ANTES: num_vistaT: #{Map.get(vista.vistaValida, :num_vista)} primario #{Map.get(vista.vistaValida, :primario)}")
                    nuevaVista = %{vista | vistaValida: Map.get(vista, :vistaTentativa)}
                    IO.puts("VALIDAR vista tras fallo DESPUES: num_vistaT: #{Map.get(nuevaVista.vistaValida, :num_vista)} primario #{Map.get(nuevaVista.vistaValida, :primario)}")
                    nuevaVista
              true ->
                     vista
            end
          true ->
            vista
          end
    end
#        if n_vista==0 do
#            #Si no hay primario y llega nodo nuevo --> nuevo primario
#            if nueva_vista.vistaTentativa.primario==:undefined do
#              nueva_vista.vistaTentativa.primario=nodo_origen
#              nueva_vista.vistaTentativa.num_vista=nueva_vista.vistaTentativa.num_vista+1
#              #Si hay primario pero no copia y llega nodo nuevo --> nuevo copia
#            else if nueva_vista.vistaTentativa.copia==:undefined
#              nueva_vista.vistaTentativa.copia=nodo_origen
#              nueva_vista.vistaTentativa.num_vista=nueva_vista.vistaTentativa.num_vista+1
#              #Llegada la copia, ya la vista es valida SOLAMENTE para el primario
#              nueva_vista.vistaValida = nueva_vista.vistaTentativa
#              nueva_
#            #else
#              #nueva_vista.vistaTentativa.nodosEspera++[nodo_origen]
#            end
#        else if n_vista!=-1
            #La vista que llega es igual que nuestra tentativa pero superior a la valida
           #if n_vista!=nueva_vista.vistaValida.num_vista and n_vista==nueva_vista.vistaTentativa.num_vista do ES REDUNDANTE?
#           if n_vista!=nueva_vista.vistaValida.num_vista and nueva_vista.vistaTentativa.copia==nodo_origen do #esto lo hace solo la copia
#               nueva_vista.vistaValida = nueva_vista.vistaTentativa
#               #si no hay copia y un nodo secundario manda un latido, se coloca como copia(porq ha habido fallo)
#           else if n_vista != nueva_vista.vistaValida.num_vista and nueva_vista.vistaTentativa.copia==:undefined
#                nueva_vista.vistaTentativa.copia = nodo_origen
#           end
#        end
#        nueva_vista
#    end

    #proceso cansino cada 50 ms
    defp procesar_situacion_servidores(vista) do
#        IO.puts("Cansino chan chan")
#        IO.inspect(Map.get(vista.situacionServidores, :latidosGV))
        #incrementa en 1 los latidos del servidor
        nuevaSituacionServidores = %{vista.situacionServidores | :latidosGV => vista.situacionServidores[:latidosGV]+1}
        nuevaVista = %{vista | situacionServidores: nuevaSituacionServidores}

        {nueva_vista, todoBien} = cond do
          nuevaVista.vistaTentativa.primario != :undefined and nuevaVista.vistaTentativa.copia != :undefined ->
             latidosPrimario = nuevaVista.situacionServidores[nuevaVista.vistaTentativa.primario]
             difPrimario = nuevaVista.situacionServidores[:latidosGV]-latidosPrimario
             latidosCopia = nuevaVista.situacionServidores[nuevaVista.vistaTentativa.copia]
             difCopia = nuevaVista.situacionServidores[:latidosGV]-latidosCopia
             IO.puts("difPrimario #{difPrimario}")
             IO.puts("difCopia #{difCopia}")
             {nueva_vista, todoBien} = cond do
               #error critico
               difPrimario >= @latidos_fallidos and difCopia >= @latidos_fallidos ->
                 #exit
                 {nuevaVista, false}
               difPrimario >= @latidos_fallidos ->
                 falloPrimario(nuevaVista)
               difCopia >= @latidos_fallidos ->
                 falloCopia(nuevaVista)
               true ->
                 {nuevaVista, true}
             end
          nuevaVista.vistaTentativa.primario != :undefined ->
           latidosPrimario = nuevaVista.situacionServidores[nuevaVista.vistaTentativa.primario]
           difPrimario = nuevaVista.situacionServidores[:latidosGV]-latidosPrimario
           IO.puts("difPrimario #{difPrimario}")
             if(difPrimario >= @latidos_fallidos) do
               {nueva_vista, todoBien} = falloPrimario(nuevaVista)
             else
               {nuevaVista, true}
             end
          nuevaVista.vistaTentativa.copia != :undefined ->
            latidosCopia = nuevaVista.situacionServidores[nuevaVista.vistaTentativa.copia]
            difCopia = nuevaVista.situacionServidores[:latidosGV]-latidosCopia
            IO.puts("difCopia #{difCopia}")
               if(difCopia >= @latidos_fallidos) do
                 {nueva_vista, todoBien} = falloCopia(nuevaVista)
               else
                   {nuevaVista, true}
               end
          true ->
            {nuevaVista, true}
        end
        nueva_vista
    end

    defp falloPrimario(vista) do
        {vista, todoBien} = cond do
            #situacion: ha caido la copia primero y no se ha validado la vista --> ERROR CRITICO
            vista.vistaTentativa != vista.vistaValida ->
                #exit
                {vista, false}
            #situacion: no hay copia --> ERROR CRITICO
            vista.vistaTentativa.copia == :undefined ->
                #exit
                {vista, false}
            true ->
                #Colocamos a la copia como nuevo primario, y si hay en espera, como copia
                nuevaVistaT = %{vista.vistaTentativa | :num_vista => vista.vistaTentativa[:num_vista]+1,
                   :primario => vista.vistaTentativa[:copia]}
                nuevaSituacionServidores = buscarNodoEspera(vista,3)

#                nueva_vista.vistaTentativa.primario = nueva_vista.vistaTentativa.copia
                #buscar nodo en espera vivo

                {vista, true}
            end
    end

    defp falloCopia(nueva_vista) do
#      nueva_vista.vistaTentativa.num_vista+1
#      #Colocamos a la copia como nuevo primario, y si hay en espera, como copia
#      nueva_vista.vistaTentativa.copia = :undefined
#
#      {nueva_vista,true}
    end

    #Busca un nodo en la lista de espera para ponerlo como copia
    defp buscarNodoEspera(vista,n) do
        var = Enum.fetch(vista.situacionServidores,n)
        IO.puts("elemento sitServidores #{var}")

        if(Map.size(vista.situacionServidores)-1>n) do
          buscarNodoEspera(vista.situacionServidores,n+1)
        end
        vista.situacionServidores
    end

    defp estaVivo(latidosGV, nodo, situacionServidores) do
      if(latidosGV-situacionServidores.nodo >= @latidos_fallidos) do
        false
      else
        true
      end
    end
end
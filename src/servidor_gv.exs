defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    #numVista,idPrimario,idCopia
    defstruct  vistaValida: %{numVista: 0, primario: :undefined, copia: :undefined},
            vistaTentativa: %{numVista: 0, primario: :undefined, copia: :undefined},
            #nodosEspera: [],
            situacion_servidores: %{:latidosP => 0, :fallosP => 0, :latidosC => 0, :fallosC => 0} #map estado servidores

    @type t_vista :: %{numVista: integer, primario: node, copia: node}

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


    defp bucle_recepcion(nueva_vista) do
    #esa vista devuelta es la actualizada en el metodo?
        nueva_vista = receive do
                   {:latido, nodo_origen, n_vista} ->
                        ### VUESTRO CODIGO
                        nueva_vista = procesa_latido(nodo_origen, n_vista, nueva_vista)
                        if(nodo_origen == nueva_vista.vistaTentativa.primario) do
                            %{map | latidosP: 1} #1 o mas, como sumarlo?
                        else if(nodo_origen == nueva_vista.vistaTentativa.primario)
                            %{map | latidosC: 1} #1 o mas
                        end
                        send(nodo_origen,{:vista_tentativa, nueva_vista.vistaTentativa,
                        nueva_vista.vistaValida.numVista==n_vista})
                        nueva_vista #para devolverlo
                
                   {:obten_vista, pid} ->
                        ### VUESTRO CODIGO
                        if nueva_vista.vistaValida.primario == :undefined do
                            IO.puts "primario indefinido"
                            send(pid, {:vista_valida, nueva_vista.vistaValida, false})
                        else
                            send(pid, {:vista_valida, nueva_vista.vistaValida,
                            nueva_vista.vistaValida.numVista==nueva_vista.vistaTentativa.numVista})
                        end
                   :procesa_situacion_servidores ->
                        ### VUESTRO CODIGO
                        todoBien = procesar_situacion_servidores(nueva_vista)
        end

        bucle_recepcion(nueva_vista) #ponerlo en el latido y obten_vista para q pueda parar el procesa?
    end

    defp procesa_latido(nodo_origen, n_vista, nueva_vista) do
        if n_vista==0 do
            #Si no hay primario y llega nodo nuevo --> nuevo primario
            if nueva_vista.vistaTentativa.primario==:undefined do
              nueva_vista.vistaTentativa.primario=nodo_origen
              nueva_vista.vistaTentativa.numVista=nueva_vista.vistaTentativa.numVista+1
              #Si hay primario pero no copia y llega nodo nuevo --> nuevo copia
            else if nueva_vista.vistaTentativa.copia==:undefined
              nueva_vista.vistaTentativa.copia=nodo_origen
              nueva_vista.vistaTentativa.numVista=nueva_vista.vistaTentativa.numVista+1
              #Llegada la copia, ya la vista es valida SOLAMENTE para el primario
              nueva_vista.vistaValida = nueva_vista.vistaTentativa
            #else
              #nueva_vista.vistaTentativa.nodosEspera++[nodo_origen]
            end
        else if n_vista<>-1
            #La vista que llega es igual que nuestra tentativa pero superior a la valida
           #if n_vista<>nueva_vista.vistaValida.numVista and n_vista==nueva_vista.vistaTentativa.numVista do ES REDUNDANTE?
           if n_vista<>nueva_vista.vistaValida.numVista and nueva_vista.vistaTentativa.copia==nodo_origen do #esto lo hace solo la copia
               nueva_vista.vistaValida = nueva_vista.vistaTentativa
               #si no hay copia y un nodo secundario manda un latido, se coloca como copia(porq ha habido fallo)
           else if n_vista <> nueva_vista.vistaValida.numVista and nueva_vista.vistaTentativa.copia==:undefined
                nueva_vista.vistaTentativa.copia = nodo_origen
           end
        end
        nueva_vista
    end

    #proceso cansino cada 50 ms
    defp procesar_situacion_servidores(nueva_vista) do
        if (map.latidosP > 0) do
            %{map | latidosP: 0}
        else
            %{map | fallosP: 1}
        end
        if (map.latidosC > 0) do
            %{map | latidosC: 0}
        else
            %{map | fallosC: 1}
        end
        if (map.fallosP == @latidos_fallidos) do
            {nueva_vista, todoBien} = falloPrimario(nueva_vista)
        else if(map.fallosC == @latidos_fallidos)
            {nueva_vista, todoBien} = falloCopia(nueva_vista)
        end
    end

    defp falloPrimario(nueva_vista) do
        #ERROR CRITICO -> PERDIDA DATOS
        {nueva_vista, todoBien} = cond do
            nueva_vista.vistaTentativa <> nueva_vista.vistaValida ->
                #STOP TODO
                #todoBien = false
                {nueva_vista, false}
            nueva_vista.vistaTentativa.copia == :undefined ->
                {nueva_vista, false}
            true ->
                nueva_vista.vistaTentativa.numVista+1
                #Colocamos a la copia como nuevo primario, y si hay en espera, como copia
                nueva_vista.vistaTentativa.primario = nueva_vista.vistaTentativa.copia
                nueva_vista.vistaTentativa.copia = :undefined
                {nueva_vista, true}
            end
    end

    defp falloCopia(nueva_vista) do
      nueva_vista.vistaTentativa.numVista+1
      #Colocamos a la copia como nuevo primario, y si hay en espera, como copia
      nueva_vista.vistaTentativa.copia = :undefined

      {nueva_vista,true}
    end
end
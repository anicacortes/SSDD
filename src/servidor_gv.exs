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
            nodosEspera: []

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
        vista = receive do
                   {:latido, nodo_origen, n_vista} ->
                        ### VUESTRO CODIGO
                        procesa_latido(nodo_origen, n_vista, nueva_vista)
                        send(nodo_origen,{:vista_tentativa, nueva_vista.vistaTentativa, true})
                
                   {:obten_vista, pid} ->
                        ### VUESTRO CODIGO
                        if nueva_vista.primario == :undefined do
                            IO.puts "primario indefinido"
                            send(pid, {:vista_valida, nueva_vista.vistaValida, false})
                        else
                            send(pid, {:vista_valida, nueva_vista.vistaValida, true})
                        end
                   :procesa_situacion_servidores ->
                
                        ### VUESTRO CODIGO
                        {}

        end

        bucle_recepcion(nueva_vista)
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
        else
          nueva_vista.vistaTentativa.nodosEspera++[nodo_origen]
        end
    else if n_vista<>-1
        #Cuando se valida una vista
       if n_vista<>nueva_vista.vistaValida.numVista and n_vista==nueva_vista.vistaTentativa.numVista do
           nueva_vista.vistaValida = nueva_vista.vistaTentativa
       end
    end
    end

    #proceso cansino
    defp procesar_situacion_servidores() do

        latido(nodo,obten_vista())

#
#        vista = %ServidorGV         #vista tiene el struct de las dos vistas
#
#        #comprueba si el idPrimario es 0(si no hay primario)
#        if elem(vista.vistaValida,1) == 0 do
#
#        end
        
    end
end
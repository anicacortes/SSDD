defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    #numVista,idPrimario,idCopia
    defstruct  vistaValida: {0,0,0}, vistaTentativa: {0,0,0}

    @tiempo_espera_carga_remota 1000

    @periodo_latido 50

    @latidos_fallidos 4 #4 o 5 latidos fallidos?


   @doc """
        Generar una estructura de datos vista inicial
    """
    def vista_inicial() do
        %ServidorGV{vistaValida: {0,0,0}, vistaTentativa: {0,0,0}}
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
        vista_inicial() #Llamar al metodo de inicio

        bucle_recepcion()
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion(vistaTentativa?) do
        ?????? = receive do
                    {:latido, nodo_origen, n_vista} ->
                
                        ### VUESTRO CODIGO
                
                    {:obten_vista, pid} ->

                        ### VUESTRO CODIGO                

                    :procesa_situacion_servidores ->
                
                        ### VUESTRO CODIGO

        end

        bucle_recepcion(nueva_vista)
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
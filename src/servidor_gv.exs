defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    # COMPLETAR  con lo campos necesarios para gestionar
    # el estado del gestor de vistas

    defstruct  ????   # A COMPLETAR

    @tiempo_espera_carga_remota 1000

    @periodo_latido 50

    @latidos_fallidos 4


   @doc """
        Generar un estructura de datos vista inicial
    """
    def vista_inicial() do
        %ServidorGV{}
    end

    @doc """
        Poner en marcha el servidor para gestión de vistas
    """
    @spec start(String.t, String.t) :: atom
    def start(host, nombre_nodo) do
        nodo = NodoRemoto.start(host, nombre_nodo,_ENV__.file,
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

        bucle_recepcion(??????????)
    end

    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido)
        init_monitor(pid_principal)
    end


    defp bucle_recepcion(???????????) do
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

    defp procesar_situacion_servidores() do

        #### VUESTRO CODIGO
        
    end
end
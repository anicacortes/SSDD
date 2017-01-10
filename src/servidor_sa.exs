
defmodule ServidorSA do
    
                
    defstruct   ....


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
        # Process.register(self(), :cliente_gv)
 


    #------------- VUESTRO CODIGO DE INICIALIZACION AQUI..........



         # Poner estado inicial
        bucle_recepcion_principal(???) 
    end


    defp bucle_recepcion_principal(???) do
        ??? = receive do

            # Solicitudes de lectura y escritura de clientes del servicio almace.
            {op, param, nodo_origen}  ->


                # ----------------- vuestro códio


        # --------------- OTROS MENSAJES QE NECESITEIS


        end

        bucle_recepcion_principal(???)
    end
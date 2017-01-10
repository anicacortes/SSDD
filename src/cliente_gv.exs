Code.require_file("#{__DIR__}/servidor_gv.exs")

defmodule ClienteGV do
    
    @tiempo_espera_de_respuesta 50
    

    @doc """
        Solicitar al cliente que envie un ping al servidor de vistas
    """
    @spec latido(node, integer) :: ServidorGV.t_vista
    def latido(nodo_servidor_gv, num_vista) do
        send({:servidor_gv, nodo_servidor_gv}, {:latido, Node.self(), num_vista})

        receive do   # esperar respuesta del ping
            {:vista_tentativa, vista, is_ok?} ->
                {vista, is_ok?}

        after @tiempo_espera_de_respuesta ->
            {ServidorGV.vista_inicial(), false}
        end
    end


    @doc """
        Solicitar al cliente que envie una petición de obtención de vista válida
    """
    @spec obten_vista(node) :: {ServidorGV.t_vista, boolean}
    def obten_vista(nodo_servidor_gv) do
       send({:servidor_gv, nodo_servidor_gv}, {:obten_vista, self()})

        receive do   # esperar respuesta del ping
            {:vista_valida, vista, is_ok?} -> {vista, is_ok?}

        after @tiempo_espera_de_respuesta  ->
            {ServidorGV.vista_inicial(), false}
        end
    end


    @doc """
        Solicitar al cliente que consiga el primario del servicio de vistas
    """
    @spec primario(node) :: node
    def primario(nodo_servidor_gv) do
        resultado = obten_vista(nodo_servidor_gv)

        case resultado do
            {vista, true} ->  vista.primario
            
            {_vista, false} -> :undefined
        end        
    end
end
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
            situacionServidores: %{:latidosGV => 0} #map estado servidores
            #nombreNodosEspera:

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
                IO.puts("BUCLE RECEPCION: num_vista #{vista.vistaTentativa.num_vista} primario #{vista.vistaTentativa.primario} copia #{vista.vistaTentativa.copia}")

                if(n_vista != 0 and n_vista != -1 and Map.fetch(vista.situacionServidores, nodo_origen) == :error) do
                  IO.puts("Proceso que ha tenido fallo de red")
                  if (vista.vistaTentativa.copia == :undefined) do
                    vista = procesa_latido(nodo_origen, 0, vista)
                  end
                end

                #Meter un nodo en la lista que gestiona los latidos (tambien se meten lo q quedan en espera)
                nueva_situacionServidores = Map.put(vista.situacionServidores, nodo_origen, Map.get(vista.situacionServidores, :latidosGV) + 1)
                nuevaVista = %{vista | situacionServidores: nueva_situacionServidores}
                nuevaV = procesa_latido(nodo_origen, n_vista, nuevaVista)
                IO.puts("DESPUES PROC_LATIDO: vistaTentativa: #{Map.get(nuevaV.vistaTentativa, :num_vista)} primario #{Map.get(nuevaV.vistaTentativa, :primario)}")
                send({:cliente_gv, nodo_origen}, {:vista_tentativa, nuevaV.vistaTentativa, true})
                nuevaV

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
#                    vistaN = %{vistaN | vistaValida: nuevaVistaT}
                    IO.puts("Despues num_vista: #{Map.get(vistaN.vistaTentativa, :num_vista)} copia #{Map.get(vistaN.vistaTentativa, :copia)}")
                    vistaN
              #El servidor no se entera de las caidas de primario y copia
              vista.vistaTentativa.primario == nodo_origen ->
                    {nuevaVista, todoBien} = falloPrimario(vista)
                    if (todoBien == false) do
                        exit(:kill)
                     end
                    nuevaVista
              vista.vistaTentativa.copia == nodo_origen ->
                    {nuevaVista, todoBien} = falloCopia(vista)
                    nuevaVista
              true ->
                    IO.puts("Se mete nodo espera..")
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
            IO.puts("Latido -1")
            vista
          end
    end

    #proceso cansino cada 50 ms
    defp procesar_situacion_servidores(vista) do
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
        if (todoBien == false) do
            exit(:kill)
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
                listNodos = List.delete(Map.keys(vista.situacionServidores), :latidosGV) |> List.delete(vista.vistaTentativa.primario) |> List.delete(vista.vistaTentativa.copia)
                #Se pasa como parametro sitServidores despues d borrar el nodo primario en map
                {nuevaSituacionServidores, nodo} = buscarNodoEspera(listNodos,
                  Map.delete(vista.situacionServidores, vista.vistaTentativa.primario),:undefined)
                IO.puts("nodo recogido: #{nodo}")
                #buscar nodo en espera vivo
                nuevaVistaT = %{vista.vistaTentativa | :num_vista => vista.vistaTentativa[:num_vista]+1,
                   :primario => vista.vistaTentativa[:copia], :copia => nodo}
                nuevaVista = %{vista | situacionServidores: nuevaSituacionServidores, vistaTentativa: nuevaVistaT}
                {nuevaVista, true}
            end
    end

    defp falloCopia(vista) do
            listNodos = List.delete(Map.keys(vista.situacionServidores), :latidosGV) |> List.delete(vista.vistaTentativa.primario) |> List.delete(vista.vistaTentativa.copia)
            #Se pasa como parametro sitServidores despues d borrar el nodo copia en map
            {nuevaSituacionServidores, nodo} = buscarNodoEspera(listNodos,
              Map.delete(vista.situacionServidores, vista.vistaTentativa.copia),:undefined)
            IO.puts("nodo recogido: #{nodo}")
            #buscar nodo en espera vivo
            nuevaVistaT = %{vista.vistaTentativa | :num_vista => vista.vistaTentativa[:num_vista]+1,
            :copia => nodo}
            nuevaVista = %{vista | situacionServidores: nuevaSituacionServidores, vistaTentativa: nuevaVistaT}
            {nuevaVista, true}
    end


    defp buscarNodoEspera([],situacionServidores, encontrado) do
    IO.puts("lista vacia d espera")
    {situacionServidores, encontrado}
    end
    #Busca un nodo en la lista de espera para ponerlo como copia. Devuelve el map con todo, y el nodo buscado
    defp buscarNodoEspera(listNodos,situacionServidores,encontrado) do
        nodo = List.first(listNodos)
        listNodos = List.delete(listNodos, nodo)
        IO.puts("elemento sitServidores #{nodo}")
        vivo = estaVivo(situacionServidores.latidosGV, situacionServidores[nodo])
        IO.puts("esta vivo? #{vivo}")
        {nuevaSituacionServidores, nuevoEncontrado} = cond do
          vivo and encontrado==:undefined ->
                {situacionServidores, nodo}
          !vivo ->
                Map.pop(situacionServidores, nodo)
                {situacionServidores, encontrado}
          true ->
                {situacionServidores, encontrado}
        end
        buscarNodoEspera(listNodos, nuevaSituacionServidores, nuevoEncontrado)
    end

    defp estaVivo(latidosGV, latidosNodo) do
      if(latidosGV-latidosNodo >= @latidos_fallidos) do
        false
      else
        true
      end
    end
end
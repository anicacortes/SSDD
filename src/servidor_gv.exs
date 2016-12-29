#####
# AUTORES: Beatriz Perez, Ana Roig
#     NIA: 683546,        686329
# Fichero:servidor_gv.exs
#  Tiempo: 25h
# Descripcion: Clase correspondiente al gestor,encargado de la deteccion y resolucion de fallos
#####

defmodule ServidorGV do

    @moduledoc """
        modulo del servicio de vistas
    """

    # Tipo estructura de dtos que guarda el estado del servidor de vistas
    defstruct  vistaValida: %{num_vista: 0, primario: :undefined, copia: :undefined},
            vistaTentativa: %{num_vista: 0, primario: :undefined, copia: :undefined},
            situacionServidores: %{:latidosGV => 0} #map estado servidores

    @type t_vista :: %{num_vista: integer, primario: node, copia: node}

    @tiempo_espera_carga_remota 1000

    @periodo_latido 50

    @latidos_fallidos 4


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

        bucle_recepcion(%ServidorGV{})
    end

    #Metodo que lo ejecuta un proceso cada 50ms
    def init_monitor(pid_principal) do
        send(pid_principal, :procesa_situacion_servidores)
        Process.sleep(@periodo_latido)
        init_monitor(pid_principal)
    end

    #Bucle principal de recepcion de todos los mensajes
    defp bucle_recepcion(vista) do
        nueva_vista = receive do
           {:latido, nodo_origen, n_vista} ->
                vista = comprobar_fallo_red(vista, n_vista, nodo_origen)
                #Meter todos nodos en la lista que gestiona los latidos e
                #incrementa numero de latidos
                nuevaSitServ = Map.put(vista.situacionServidores, nodo_origen,
                   Map.get(vista.situacionServidores, :latidosGV) + 1)
                nuevaVista = %{vista | situacionServidores: nuevaSitServ}
                vista = procesa_latido(nodo_origen, n_vista, nuevaVista)
                IO.puts("vTentativa: nVista #{Map.get(vista.vistaTentativa, :num_vista)
                } primario #{Map.get(vista.vistaTentativa, :primario)
                } copia #{Map.get(vista.vistaTentativa, :copia)}")
                send({:cliente_gv, nodo_origen}, {:vista_tentativa, vista.vistaTentativa, true})
                vista

           {:obten_vista, pid} ->
                if vista.vistaValida.primario == :undefined do
                    IO.inspect "Primario indefinido"
                    send(pid, {:vista_valida, vista.vistaValida, false})
                else
                    send(pid, {:vista_valida, vista.vistaValida,
                    vista.vistaValida.num_vista==vista.vistaTentativa.num_vista})
                end
                vista

           :procesa_situacion_servidores ->
                procesar_situacion_servidores(vista)
        end
        bucle_recepcion(nueva_vista)
    end

    #Comprueba si se ha producido un fallo de red porque llega ping de un nodo
    #que no esta en espera
    defp comprobar_fallo_red(vista, n_vista, nodo_origen) do
        if(n_vista != 0 and n_vista != -1 and Map.fetch(vista.situacionServidores,
           nodo_origen) == :error) do
            IO.puts("Fallo de red")
            if (vista.vistaTentativa.copia == :undefined) do
                vista = procesa_latido(nodo_origen, 0, vista) #mirar si hay en espera
            end
        end
        vista
    end

    #Modificacion de las vistas en funcion de la situacion actual
    defp procesa_latido(nodo_origen, n_vista, vista) do
       cond do
          n_vista==0 ->
            cond do
              vista.vistaTentativa.primario == :undefined ->
                    IO.puts("Añadido primario")
                    nuevaVistaT = %{vista.vistaTentativa | num_vista: Map.get(
                     vista.vistaTentativa, :num_vista) + 1, primario: nodo_origen}
                    %{vista | vistaTentativa: nuevaVistaT}
              vista.vistaTentativa.copia == :undefined ->
                    IO.puts("Añadida copia")
                    nuevaVistaT = %{vista.vistaTentativa | num_vista: Map.get(
                     vista.vistaTentativa, :num_vista) + 1, copia: nodo_origen}
                    %{vista | vistaTentativa: nuevaVistaT}

              #El servidor no se entera de las caidas de primario y copia
              vista.vistaTentativa.primario == nodo_origen ->
                    caida_inesperada_primario(vista)
              vista.vistaTentativa.copia == nodo_origen ->
                    caida_inesperada_copia(vista)
              true ->
                    IO.puts("Añadido nodo espera")
                    vista
            end
          n_vista != -1 ->
            cond do
              n_vista != vista.vistaValida.num_vista and vista.vistaTentativa.primario
                == nodo_origen -> #esto lo hace solo la copia
                    IO.puts("Se ha validado la vista")
                    %{vista | vistaValida: Map.get(vista, :vistaTentativa)}
              true ->
                     vista
            end
          true ->
            vista
        end
    end

    #Incrementa nº latidos del GV y comprueba si ha habido fallo
    #Para el sistema en caso de fallo critico
    defp procesar_situacion_servidores(vista) do
        #incrementa en 1 los latidos del servidor
        nuevaSitSer = %{vista.situacionServidores | :latidosGV =>
          vista.situacionServidores[:latidosGV]+1}
        nuevaVista = %{vista | situacionServidores: nuevaSitSer}

        {nueva_vista, todoBien} = cond do
          nuevaVista.vistaTentativa.primario != :undefined and
            nuevaVista.vistaTentativa.copia != :undefined ->
                difLatido_primario_copia(nuevaVista)
          nuevaVista.vistaTentativa.primario != :undefined ->
                difLatido_primario(nuevaVista)
          nuevaVista.vistaTentativa.copia != :undefined ->
                difLatido_copia(nuevaVista)
          true ->
                {nuevaVista, true}
        end
        if (todoBien == false) do
            exit(:kill)
        end
        nueva_vista
    end

    #Comprueba diferencia de latidos de primario y copia para ver si ha
    #habido algun fallo
    defp difLatido_primario_copia(vista) do
        latidosPrimario = vista.situacionServidores[vista.vistaTentativa.primario]
        difPrimario = vista.situacionServidores[:latidosGV]-latidosPrimario
        latidosCopia = vista.situacionServidores[vista.vistaTentativa.copia]
        difCopia = vista.situacionServidores[:latidosGV]-latidosCopia
        IO.puts("difPrimario #{difPrimario} difCopia #{difCopia}")
        cond do
            difPrimario >= @latidos_fallidos and difCopia >= @latidos_fallidos ->
                {vista, false} #fallo critico
            difPrimario >= @latidos_fallidos -> falloPrimario(vista)
            difCopia >= @latidos_fallidos -> falloCopia(vista)
            true -> {vista, true}
        end
    end

    #Comprueba diferencia de latidos de primario para ver si hay algun fallo
    defp difLatido_primario(vista) do
        latidosPrimario = vista.situacionServidores[vista.vistaTentativa.primario]
        difPrimario = vista.situacionServidores[:latidosGV]-latidosPrimario
        IO.puts("difPrimario #{difPrimario}")
        if(difPrimario >= @latidos_fallidos) do
           falloPrimario(vista)
        else
           {vista, true}
        end
    end

    #Comprueba diferencia de latidos de copia para ver si hay algun fallo
    defp difLatido_copia(vista) do
        latidosCopia = vista.situacionServidores[vista.vistaTentativa.copia]
        difCopia = vista.situacionServidores[:latidosGV]-latidosCopia
        IO.puts("difCopia #{difCopia}")
        if(difCopia >= @latidos_fallidos) do
           falloCopia(vista)
        else
           {vista, true}
        end
    end

    #Solucionar fallo en primario segun la situacion
    defp falloPrimario(vista) do
        cond do
            vista.vistaTentativa != vista.vistaValida ->
                {vista, false}  #no se ha validado la vista --> ERROR
            vista.vistaTentativa.copia == :undefined ->
                {vista, false} #no hay copia --> ERROR
            true ->
                actualizar_vTentativa_fallo(vista, :primario)
        end
    end

    #Solucionar fallo en copia segun la situacion
    defp falloCopia(vista) do
        actualizar_vTentativa_fallo(vista, :copia)
    end

   #Actualizar lista nodos eliminando caidos y vista tentativa
    defp actualizar_vTentativa_fallo(vista, nodoPC) do
        listNodos = List.delete(Map.keys(vista.situacionServidores), :latidosGV)
          |> List.delete(vista.vistaTentativa.primario)
          |> List.delete(vista.vistaTentativa.copia)
        #Se pasa como parametro sitServidores despues d borrar el nodo primario en map
        {nuevaSituacionServidores, nodo} = buscarNodoEspera(listNodos,
          Map.delete(vista.situacionServidores, vista.vistaTentativa[nodoPC]),:undefined)
        #buscar nodo en espera vivo
        nuevaVistaT = case nodoPC do
          :primario ->
              %{vista.vistaTentativa | :num_vista => vista.vistaTentativa[:num_vista]+1,
                 :primario => vista.vistaTentativa[:copia], :copia => nodo}
          :copia ->
              %{vista.vistaTentativa | :num_vista => vista.vistaTentativa[:num_vista]+1,
                 :copia => nodo}
        end
        {%{vista | situacionServidores: nuevaSituacionServidores, vistaTentativa:
          nuevaVistaT}, true}
    end

    #Busca un nodo en la lista de espera para ponerlo como copia
    #Devuelve undefined si no encuentra ninguno
    defp buscarNodoEspera([],situacionServidores, encontrado) do
        {situacionServidores, encontrado}
    end
    defp buscarNodoEspera(listNodos,situacionServidores,encontrado) do
        nodo = List.first(listNodos)
        listNodos = List.delete(listNodos, nodo)
        vivo = estaVivo(situacionServidores.latidosGV, situacionServidores[nodo])
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

    #Devuelve true si el nodo sigue vivo, false en caso contrario
    defp estaVivo(latidosGV, latidosNodo) do
      if(latidosGV-latidosNodo >= @latidos_fallidos) do
        false
      else
        true
      end
    end

    #Gestion de un fallo del primario que no ha detectado el GV,
    #se trata como un fallo normal
    defp caida_inesperada_primario(vista) do
        IO.puts("Caida inesperada primario")
        {nuevaVista, todoBien} = falloPrimario(vista)
        if (todoBien == false) do
          exit(:kill)
        end
        nuevaVista
    end

    #Gestion de un fallo de la copia que no ha detectado el GV,
    #se trata como un fallo normal
    defp caida_inesperada_copia(vista) do
        IO.puts("Caida inesperada copia")
        {nuevaVista, _todoBien} = falloCopia(vista)
        nuevaVista
    end
end
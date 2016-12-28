# Compilar y cargar ficheros con modulos necesarios
Code.require_file("#{__DIR__}/nodo_remoto.exs")
Code.require_file("#{__DIR__}/servidor_gv.exs")
Code.require_file("#{__DIR__}/cliente_gv.exs")

#Poner en marcha el servicio de tests unitarios con tiempo de vida limitada
# seed: 0 para que la ejecucion de tests no tenga orden aleatorio
ExUnit.start([timeout: 20000, seed: 0]) # milisegundos

defmodule  GestorVistasTest do

    use ExUnit.Case

    # @moduletag timeout 100  para timeouts de todos lo test de este modulo

    @host1 "127.0.0.1"

    @latidos_fallidos 4

    @intervalo_latido 50


    setup_all do
        # Poner en marcha nodos cliente y servidor
        #sv = :"sv@127.0.0.1"
        # c1 = :"c1@127.0.0.1";
        # c2 = :"c2@127.0.0.1";
        c3 = :"c3@127.0.0.1"
        sv = ServidorGV.start(@host1, "sv")
        c1 = ClienteGV.start(@host1, "c1", sv)
        c2 = ClienteGV.start(@host1, "c2", sv)
        c3 = ClienteGV.start(@host1, "c3", sv)

        on_exit fn ->
                    #eliminar_nodos(sv, c1, c2, c3)
                    IO.puts "Finalmente eliminamos nodos"
                    NodoRemoto.stop(sv)
                    NodoRemoto.stop(c1)
                    NodoRemoto.stop(c2)        
                    NodoRemoto.stop(c3)                            
                end

        {:ok, [sv: sv, c1: c1, c2: c2, c3: c3]}
    end


    # Primer test : un primer primario
    test "Primario prematuro", %{c1: c1} do
        IO.puts("Test: Primario prematuro ...")

        p = ClienteGV.primario(c1)

        assert p == :undefined

        IO.puts(" ... Superado")
    end


    # Segundo test : primer nodo copia
    test "Primer primario", %{c1: c} do
        IO.puts("Test: Primer primario ...")

        primer_primario(c, @latidos_fallidos * 2)
        comprobar_tentativa(c, c, :undefined, 1)
        
        IO.puts(" ... Superado")
    end


    # Tercer test primer_nodo_copia
    test "Primer nodo copia", %{c1: c1, c2: c2} do
        IO.puts("Test: Primer nodo copia ...")

        {vista, _} = ClienteGV.latido(c1, -1)  # Solo interesa vista tentativa
        primer_nodo_copia(c1, c2, @latidos_fallidos * 2)

        # validamos nueva vista por estar completa
        ClienteGV.latido(c1, vista.num_vista + 1)

        comprobar_valida(c1, c1, c2, vista.num_vista + 1)

        IO.puts(" ... Superado")
    end


    ## Test 3 : Después, Copia (C2) toma el relevo si Primario falla.,
    test "Copia releva primario", %{c2: c2} do
        IO.puts("Test: copia toma relevo si primario falla ...")

        {vista, _} = ClienteGV.latido(c2, 2)
        copia_releva_primario(c2, vista.num_vista, @latidos_fallidos * 2)
        
        comprobar_tentativa(c2, c2, :undefined, vista.num_vista + 1)

        IO.puts(" ... Superado")        
    end

    ## Test 4 : Servidor rearrancado (C1) se convierte en copia.
    test "Servidor rearrancado se convierte en copia", %{c1: c1, c2: c2} do
        IO.puts("Test: Servidor rearrancado se convierte en copia ...")

        {vista, _} = ClienteGV.latido(c2, 2)   # Solo interesa vista tentativa
        servidor_rearranca_a_copia(c1, c2, 2, @latidos_fallidos * 2)

        # validamos nueva vista por estar DE NUEVO completa
        ClienteGV.latido(c2, vista.num_vista + 1)

        comprobar_valida(c2, c2, c1, vista.num_vista + 1)

        IO.puts(" ... Superado")
     end

    ## Test 5 : 3er servidor en espera (C3) se convierte en copia
    ##          si primario falla.
    test "Servidor en espera se convierte en copia por fallo de primario", %{c1: c1, c2: c2, c3: c3} do
        IO.puts("Test:Servidor en espera se convierte en copia por fallo de primario ...")

        {vista, _} = ClienteGV.latido(c3, 0)
        espera_a_copia(c1, c2, c3, vista.num_vista)
        #c1 pasa a ser primario y valida la vista
        ClienteGV.latido(c1, vista.num_vista + 1)
        comprobar_valida(c1, c1, c3, vista.num_vista + 1)

         IO.puts(" ... Superado")

    end


    ## Test 6 : Primario rearrancado (C2) es tratado como caido.
     test "Primario rearrancado (C2) es tratado como caido", %{c1: c1, c2: c2, c3: c3} do
         IO.puts("Test:Primario rearrancado (C2) es tratado como caido ...")

         {vistaAnterior, _} = ClienteGV.latido(c3, -1)

         {vistaPosterior, _} = ClienteGV.latido(c2, 0)

         comprobar_tentativa(c2, c1, c3, vistaAnterior.num_vista)
         IO.puts(" ... Superado")
     end

    #Situacion previa: numVista 5 primario c1 copia c3 espera c2
    ## Test 7 : Servidor de vistas espera a que primario confirme vista
    ##          pero este no lo hace.
    ##          Poner C3 como Primario, C1 como Copia, C2 para comprobar
    ##          - C3 no confirma vista en que es primario,
    ##          - Cae, pero C1 no es promocionado porque C3 no confimo !
#    test "Servidor de vistas espera a que primario confirme vista pero este no lo hace.", %{c1: c1, c2: c2, c3: c3} do
#        IO.puts("Test:Servidor de vistas espera a que primario confirme vista pero este no lo hace ...")
#        {vista, _} = ClienteGV.latido(c3, -1)
#        #Tiramos al primario c1
#        espera_releva_copia(c3, c2, vista.num_vista, @latidos_fallidos * 2)
#        #Metemos a c1 como espera
#        ClienteGV.latido(c1, 0)
#        #Ahora hay que tirar a c2 que esta como copia
#        espera_releva_copia(c3, c1, vista.num_vista, @latidos_fallidos * 2)
#        {vista, _} = ClienteGV.latido(c2, 0)
#        #Situacion del test
#        espera_releva_copia(c1, c2, vista.num_vista, @latidos_fallidos * 2)
#        {vista, coincide} = ClienteGV.obten_vista(c1)
#        assert coincide==false
#         IO.puts(" ... Superado")
#        end

    #Situacion previa: numVista 5 primario c1 copia c3 espera c2
    ## Test 8 : Si anteriores servidores caen (Primario  y Copia),
    ##       un nuevo servidor sin inicializar no puede convertirse en primario.
    # sin_inicializar_no(C1, C2, C3),
#    test "Si anteriores servidores caen, nuevo servidor sin inicializar no puede ser primario", %{c1: c1, c2: c2, c3: c3} do
#            IO.puts("Test: Si anteriores servidores caen, nuevo servidor sin inicializar no puede ser primario ...")
#            {vista, _} = ClienteGV.latido(c2, 0)    #Poner c3 en espera
#            #Esperamos a que caigan ambos servidores, primario y copia
#            caer_primario_secundario(c2, vista.num_vista, @latidos_fallidos * 2)
#             IO.puts(" ... Superado")
#    end

    #Situacion previa: numVista 5 primario c1 copia c3 espera c2
    #Fallo de red
#    test "Fallo de red, sustituye al primario", %{c1: c1, c2: c2, c3: c3} do
#         IO.puts("Test: Fallo de red, sustituye al primario ...")
#
#         {vistaRetrasada, _} = ClienteGV.latido(c1, -1)
#
#         #espera fallo del primario c1
#         espera_releva_copia(c3, c2, vistaRetrasada.num_vista, @latidos_fallidos * 2)
#
#         {vista, _} = ClienteGV.latido(c3, vistaRetrasada.num_vista+1) #valida
#         #cae copia y no hay en espera
#         espera_releva_copia(c3, c3, vista.num_vista, @latidos_fallidos * 2)
#         comprobar_caido(c1, c3, c1, vista.num_vista, vistaRetrasada)
#         IO.puts(" ... Superado")
#    end

    #Situacion previa: numVista 5 primario c1 copia c3 espera c2
    #Caida rapida de un servidor (envia latido 0)
    test "Fallo del servidor sin que lo detecte GV", %{c1: c1, c2: c2, c3: c3} do
         IO.puts("Test: Fallo del servidor sin que lo detecte GV...")

         {vista, _} = ClienteGV.latido(c1, 0) #FALLO
         Process.sleep(@intervalo_latido)
         #valida el nuevo primario
         {vista, _} = ClienteGV.latido(c3, vista.num_vista + 1)

         comprobar_valida(c3, c3, c2, vista.num_vista)
         IO.puts(" ... Superado")
    end


    # ------------------ FUNCIONES DE APOYO A TESTS ------------------------

    defp primer_primario(_c, 0) do :fin end
    defp primer_primario(c, x) do

        {vista, _} = ClienteGV.latido(c, 0)
        IO.puts("PRIMER PRIMARIO: numVista #{vista.num_vista} primario #{vista.primario} copia #{vista.copia}")
        if vista.primario != c do
            Process.sleep(@intervalo_latido)
            primer_primario(c, x - 1)
        end
    end

    defp primer_nodo_copia(_c1, _c2, 0) do :fin end
    defp primer_nodo_copia(c1, c2, x) do

        # != 0 para no dar por nuevo y < 0 para no validar
        ClienteGV.latido(c2, -1)  
        {vista, _} = ClienteGV.latido(c2, 0)

        if vista.copia != c2 do
            Process.sleep(@intervalo_latido)
            primer_nodo_copia(c1, c2, x - 1)
        end
    end

    def copia_releva_primario( _, _num_vista_inicial, 0) do :fin end
    def copia_releva_primario(c2, num_vista_inicial, x) do

        {vista, _} = ClienteGV.latido(c2, num_vista_inicial)

        if (vista.primario != c2) or (vista.copia != :undefined) do
            Process.sleep(@intervalo_latido)
            copia_releva_primario(c2, num_vista_inicial, x - 1)
        end

    end

    defp servidor_rearranca_a_copia(_c1, _c2, _num_vista_inicial, 0) do :fin end
    defp servidor_rearranca_a_copia(c1, c2, num_vista_valida, x) do

        ClienteGV.latido(c1, 0)
        {vista, _} = ClienteGV.latido(c2, num_vista_valida)

        if vista.copia != c1 do
            Process.sleep(@intervalo_latido)
            primer_nodo_copia(c1, c2, x - 1)
        end
    end

    defp comprobar_caido(nodo_cliente, nodo_primario, nodo_copia, n_vista, n_vistaCaido) do
        # Solo interesa vista tentativa
        {vista, _} = ClienteGV.latido(nodo_cliente,n_vistaCaido) # se levanta el caido y se pone en espera
        IO.puts("COMP CAIDO: numVista #{vista.num_vista} primario #{vista.primario} copia #{vista.copia}")
        comprobar(nodo_primario, nodo_copia, vista.num_vista, vista)
    end

    defp comprobar_tentativa(nodo_cliente, nodo_primario, nodo_copia, n_vista) do
        # Solo interesa vista tentativa
        {vista, _} = ClienteGV.latido(nodo_cliente, -1) 
        IO.puts("COMP TENTATIVA: numVista #{vista.num_vista} primario #{vista.primario} copia #{vista.copia}")
        comprobar(nodo_primario, nodo_copia, n_vista, vista)        
    end


    defp comprobar_valida(nodo_cliente, nodo_primario, nodo_copia, n_vista) do
        {vista, _ } = ClienteGV.obten_vista(nodo_cliente)

        comprobar(nodo_primario, nodo_copia, n_vista, vista)

        assert ClienteGV.primario(nodo_cliente) == nodo_primario
    end


    defp comprobar(nodo_primario, nodo_copia, n_vista, vista) do
        IO.puts("nodoPrimario #{nodo_primario} vistaPrimario #{vista.primario}")
        assert vista.primario == nodo_primario 

        assert vista.copia == nodo_copia 

        assert vista.num_vista == n_vista
    end

    defp espera_a_copia(c1, c2, c3, num_vista_inicial) do
        {vista, _} = ClienteGV.latido(c3, num_vista_inicial)

        espera_releva_copia(c1, c3, vista.num_vista, @latidos_fallidos * 2) #provoca caida primario

    end

    defp espera_releva_copia(_c2, _c3, num_vista_inicial, 0) do :fin end
    defp espera_releva_copia(c2, c3, num_vista_inicial, x) do
        {vista, _} = ClienteGV.latido(c2, num_vista_inicial)
        ClienteGV.latido(c3, num_vista_inicial)
        #Si no ha habido un fallo, se vuelve a mandar latidos de copia y espera
        if (vista.primario != c2) or (vista.copia != c3) do
          Process.sleep(@intervalo_latido)
          espera_releva_copia(c2, c3, num_vista_inicial, x - 1)
        end
    end

    defp caer_primario_secundario(_c3, num_vista_inicial, 0) do :fin end
    defp caer_primario_secundario(c3, num_vista_inicial, x) do
        ClienteGV.latido(c3, num_vista_inicial)
        Process.sleep(@intervalo_latido)
        caer_primario_secundario(c3, num_vista_inicial, x - 1)
    end
end

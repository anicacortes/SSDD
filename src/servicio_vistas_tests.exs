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
        #c3 = ClienteGV.start(@host1, "c3", sv)

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
    test "primario_prematuro", %{c1: c1} do
        IO.puts("Test: Primario prematuro ...")

        p = ClienteGV.primario(c1)

        assert p == :undefined

        IO.puts(" ... Superado")
    end


    # Segundo test : primer nodo copia
    test "primer_primario", %{c1: c} do
        IO.puts("Test: Primer primario ...")

        primer_primario(c, @latidos_fallidos * 2)
        comprobar(c, c, :undefined, 1)
        
        IO.puts(" ... Superado")
    end


    # 
    test "primer_nodo_copia", %{c1: c1, c2: c2} do
        IO.puts("Test: Primer nodo copia ...")

        {vista, _is_ok} = ClienteGV.obten_vista(c1)
        primer_nodo_copia(c1, c2, @latidos_fallidos * 2)
        comprobar(c1, c1, c2, vista.num_vista + 1)

        IO.puts(" ... Superado")
    end


    ## Test 3 : Después, Copia (C2) toma el relevo si Primario falla.
    # relevo_copia(C1, C2),

    ## Test 4 : Servidor rearrancado (C1) se convierte en copia.
    # relevo_copia(C1, C2),

    ## Test 5 : 3er servidor en espera (C3) se convierte en copia
    ##          si primario falla.
    # espera_a_copia(C1, C2, C3),

    ## Test 6 : Primario rearrancado (C2) es tratado como caido.
    # rearrancado_caido(C1, C3),

    ## Test 7 : Servidor de vistas espera a que primario confirme vista
    ##          pero este no lo hace.
    ##          Poner C3 como Primario, C1 como Copia, C2 para comprobar
    ##          - C3 no confirma vista en que es primario,
    ##          - Cae, pero C1 no es promocionado porque C3 no confimo !
    # primario_no_confirma_vista(C1, C2, C3),

    ## Test 8 : Si anteriores servidores caen (Primario  y Copia),
    ##       un nuevo servidor sin inicializar no puede convertirse en primario.
    # sin_inicializar_no(C1, C2, C3),


    # ------------------ FUNCIONES DE APOYO A TESTS ------------------------

    defp primer_primario(_c, 0) do :fin end
    defp primer_primario(c, x) do
        {vista, _is_ok} = ClienteGV.latido(c, 0)

        if vista.primario != c do
            Process.sleep(@intervalo_latido)
            primer_primario(c, x - 1)
        end
    end

    defp primer_nodo_copia(_c1, _c2, 0) do :fin end
    defp primer_nodo_copia(c1, c2, x) do
        ClienteGV.latido(c1, 1)
        {vista, _is_ok} = ClienteGV.latido(c2, 0)

        if vista.copia != c2 do
            Process.sleep(@intervalo_latido)
            primer_nodo_copia(c1, c2, x - 1)
        end
    end


    defp comprobar(nodo_cliente, nodo_primario, nodo_copia, num_vista) do
        {vista, _is_ok} = ClienteGV.obten_vista(nodo_cliente)

        assert vista.primario == nodo_primario 

        assert vista.copia == nodo_copia 

        assert vista.num_vista == num_vista 

        assert ClienteGV.primario(nodo_cliente) == nodo_primario
    end


end

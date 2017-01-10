# Ejecución del programa de tests
elixir  --name maestro@127.0.0.1 --cookie 'palabrasecreta' \
	--erl  '-kernel inet_dist_listen_min 32000' \
	--erl  '-kernel inet_dist_listen_max 32009' \
	servicio_almacenamiento_tests.exs

# Eliminar restos de ejcución de VMs Erlang
# Para ejecución distribuida poner las @ IP de las maquins físicas
#  y duplicar tantas lineas ssh como máquinas
HOST1='127.0.0.1'
ssh HOST1 'pkill erl; pkill erl; pkill erl; pkill epmd'

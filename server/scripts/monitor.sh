order_socket=$(ps aux | grep -c "wireless_order_socket.jar")
if [ "$order_socket" -gt 1 ]; then
	if [ "$1" != "" ]; then
		exec 3<>/dev/tcp/127.0.0.1/33333
		echo -e "$1">&3
		cat <&3
	else
		echo "Usage: monitor.sh [command]"
		echo "command arguements are listed below"
		echo "start_monitor [-t interval]	start to run the monitor, it would monitor"
		echo "                                the status every specific time (set by interval)"
		echo "                                the value of interval must be greater than zero"
		echo "kill_monitor	stop monitoring the socket's status"
		echo "kill_socket	nice way to stop all the threads running"
		echo "check_version   get the version of this server socket"
		echo "                in socket so that make the socket exit normally"
	fi
else
	echo "Wireless order socket hasn't started."
fi
exit 0

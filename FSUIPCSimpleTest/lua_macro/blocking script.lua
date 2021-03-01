-- sample script that will do infinite loop, to test kill functionality using FSUIPC Java SDK
while 1 do
	ipc.log("This is blocking script!")
	ipc.sleep(750)
end 
--flag and param test script
ipc.log("*** Flag/Param test lua script started ***")

function on_param(param)
	ipc.log("Received param: "..param)
end

function on_flag(flag)
	ipc.log("On flag")
	ipc.log("Flag: "..flag.." value is now: "..tostring(ipc.testflag(flag)))
end

function on_kill_all(offset, value)
	ipc.log("Called on kill all. Offset is: "..offset.." , value is: "..value)
end

event.param("on_param")
event.flag("on_flag")

event.offset(0x3110, "DD", 8, "on_kill_all") 
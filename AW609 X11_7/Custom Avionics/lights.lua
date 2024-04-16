-- lights
-- Version: aw-609
----------------------------------------------------------------------------------------------------
defineProperty("land_light", globalPropertyi("sim/cockpit2/switches/landing_lights_on"))
defineProperty("taxi_light", globalPropertyi("sim/cockpit2/switches/taxi_light_on"))
defineProperty("land_switches", globalPropertyi("sim/cockpit2/switches/landing_lights_switch[1]"))

set(land_light,0)--0=off,1=on
set(taxi_light,1)--0=off,1=on
set(land_switches,1)--0=off,1=on



function update()
	---light land and taxi 
	if (get(land_light)==1) then
			set(taxi_light,0)
end
                 ---light land 2 and taxi 
	if (get(land_light)==0) then
			set(land_switches,0)


end
end

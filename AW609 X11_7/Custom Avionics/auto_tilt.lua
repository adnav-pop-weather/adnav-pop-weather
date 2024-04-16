
defineProperty("angle",globalPropertyf("sim/cockpit2/controls/thrust_vector_ratio"))
defineProperty("tilt_mode", globalPropertyi("sim/cockpit2/switches/custom_slider_on[8]"))
defineProperty("rdr_alt",    globalPropertyf("sim/cockpit2/gauges/indicators/radio_altimeter_height_ft_pilot"))

set(angle,.88)

function update()
	
		if(get(rdr_alt)>130)then
							
		if(get(tilt_mode)==1)then--AUTO

			set(angle,get(angle))
		
		if(get(angle)>.00)then set(angle,0.00)
end
end
end
end
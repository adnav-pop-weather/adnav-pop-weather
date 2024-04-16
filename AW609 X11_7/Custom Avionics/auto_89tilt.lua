
defineProperty("angle",globalPropertyf("sim/cockpit2/controls/thrust_vector_ratio"))
defineProperty("tilt88", globalPropertyi("sim/cockpit2/switches/custom_slider_on[7]"))
defineProperty("rdr_alt",    globalPropertyf("sim/cockpit2/gauges/indicators/radio_altimeter_height_ft_pilot"))


function update()
	
		if(get(rdr_alt)>130)then
							
		if(get(tilt88)==1)then--AUTO

			set(angle,get(angle))
		
		if(get(angle)<.88)then set(angle,.88)

end
end
end
end
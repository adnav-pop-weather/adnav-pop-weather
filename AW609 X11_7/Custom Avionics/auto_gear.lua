-- Auto gear
-- Version: aw-609
----------------------------------------------------------------------------------------------------
defineProperty("cs5",globalPropertyi("sim/cockpit2/switches/custom_slider_on[5]"))--AUTO_GEAR
defineProperty("gear_handle",globalPropertyi("sim/cockpit2/controls/gear_handle_down"))
defineProperty("rdr_alt",    globalPropertyf("sim/cockpit2/gauges/indicators/radio_altimeter_height_ft_pilot"))


set(cs5,0)

function update()


--Landing gear handle logic
if(get(cs5)==1)then--auto mode	
		set(gear_handle,1)
	if(get(rdr_alt)>270)  then 
		 set(gear_handle,0)
			elseif(get(rdr_alt)<270)  then 
		 set(gear_handle,1)
elseif (get(cs5)==0)then--manual_mode
                                      set(gear_handle,get(gear_handle))	
elseif(get(cs5)==1)then--auto_mode
		set(gear_handle,1)
	end
end
end
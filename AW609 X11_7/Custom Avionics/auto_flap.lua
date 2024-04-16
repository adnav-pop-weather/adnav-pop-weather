-- Auto flaps
-- Version: aw-609
----------------------------------------------------------------------------------------------------
defineProperty("flap_mode",  globalPropertyi("sim/cockpit2/switches/custom_slider_on[10]"))
defineProperty("flap_rqst", globalPropertyf("sim/flightmodel/controls/flaprqst"))
defineProperty("ias",       globalPropertyf("sim/flightmodel/position/indicated_airspeed"))

set(flap_mode,1)--1=open,0=closed

function update()
	
	if(get(flap_mode)==1)then--auto mode	

		set(flap_rqst,1)
   	if (get(ias)<10) then
			set(flap_rqst,1.0)--2
		--TAS switch
		elseif (get(ias)>10) and (get(ias)<20) then
				set(flap_rqst,.8)--1
elseif (get(ias)>20) and (get(ias)<30) then
				set(flap_rqst,.6)--1
elseif (get(ias)>30) and (get(ias)<40) then
				set(flap_rqst,.5)--1
elseif (get(ias)>40) and (get(ias)<50) then
				set(flap_rqst,.4)--1
elseif (get(ias)>50) and (get(ias)<60) then
				set(flap_rqst,.3)--1
		elseif (get(ias)>60) then
				set(flap_rqst, 0)--0	
elseif (get(flap_mode)==0)then--manual mode
				set(flap_rqst,get(flap_rqst))

end	
end
end

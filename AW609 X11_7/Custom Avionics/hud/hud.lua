-- Head Up Display for aw-609
 -- -- -- -- -- -- -- -- -- -- --
size = { 2048, 2048 }

-- fonts
local fontSmall  = loadFont('Console12.fnt')
local fontMedium = loadFont('Console14.fnt')
local fontLarge  = loadFont('Console16.fnt')
local fontHuge   = loadFont('Console18.fnt')
local fontMax   = loadFont('Console20.fnt')

-- datarefs
defineProperty("ias",		 globalPropertyf("sim/flightmodel/position/indicated_airspeed"))
defineProperty("mag_hdg",    globalPropertyf("sim/cockpit2/gauges/indicators/heading_AHARS_deg_mag_pilot"))
defineProperty("altitude",   globalPropertyf("sim/cockpit2/gauges/indicators/altitude_ft_pilot"))
defineProperty("rdr_alt",    globalPropertyf("sim/cockpit2/gauges/indicators/radio_altimeter_height_ft_pilot"))
 defineProperty("rdr_dh",   globalPropertyf("sim/cockpit/misc/radio_altimeter_minimum"))
defineProperty("theta", 	 globalPropertyf("sim/flightmodel/position/theta"))
defineProperty("roll",       globalPropertyf("sim/cockpit2/gauges/indicators/roll_AHARS_deg_pilot"))
defineProperty("aoa",        globalPropertyf("sim/flightmodel2/misc/AoA_angle_degrees"))--read only
defineProperty("beta",  	 globalPropertyf("sim/flightmodel/position/beta"))-- yaw path
defineProperty("vvi",        globalPropertyf("sim/cockpit2/gauges/indicators/vvi_fpm_pilot"))
defineProperty("g_force",    globalPropertyf("sim/flightmodel2/misc/gforce_normal"))
defineProperty("mach",       globalPropertyf("sim/flightmodel/misc/machno"))
defineProperty("wind_dir",   globalPropertyf("sim/cockpit2/gauges/indicators/wind_heading_deg_mag"))
defineProperty("wind_spd",   globalPropertyf("sim/cockpit2/gauges/indicators/wind_speed_kts"))
defineProperty("gear_handle",globalPropertyi("sim/cockpit2/controls/gear_handle_down"))
defineProperty("spd_brk",    globalPropertyf("sim/cockpit2/controls/speedbrake_ratio"))
defineProperty("prk_brk",    globalPropertyf("sim/cockpit2/controls/parking_brake_ratio"))
--------defineProperty("guns_armed", globalPropertyi("sim/cockpit/weapons/guns_armed"))
-------defineProperty("miss_armed", globalPropertyi("sim/cockpit/weapons/missiles_armed"))
------defineProperty("bomb_armed", globalPropertyi("sim/cockpit/weapons/bombs_armed"))
defineProperty("brightness", globalPropertyf("sim/cockpit2/electrical/instrument_brightness_ratio"))
defineProperty("hud_on",     globalPropertyi("sim/cockpit2/switches/HUD_on"))
defineProperty("lon",       globalPropertyf("sim/flightmodel/position/longitude"))--read only
defineProperty("lat",       globalPropertyf("sim/flightmodel/position/latitude"))--read only
defineProperty("fuel0",      globalPropertyf("sim/cockpit2/fuel/fuel_quantity[0]"))-----
defineProperty("fuel1",      globalPropertyf("sim/cockpit2/fuel/fuel_quantity[1]"))-----
defineProperty("fuel2",      globalPropertyf("sim/cockpit2/fuel/fuel_quantity[2]"))-----
defineProperty("Vec", globalPropertyf("sim/cockpit2/controls/thrust_vector_ratio"))
defineProperty("Gaz", globalPropertyf("sim/cockpit2/engine/actuators/throttle_ratio"))


-- texts
local txtMaghdg;
local txtIas;
local txtAltitude;
local txtVVI;
local txtG;
local txtMag;
local txtRadAlt;
local txtMach;
local txtAoA;
local txtLon;
local txtLat;

-- constants

local ToffsetX =670;
local ToffsetY = -450;
local offsetX = 516;
local offsetY = -50;
local width = 300;
local height = 380;
local increment = 75;

local centerX = ToffsetX + (width/2);
local centerY = ToffsetY + (height/2);

local red = 0;
local green = 1;
local blue = 0;
local alpha = 1;

-- local vars
local direction = 0;
local gps_dir = 0;

set(Vec,.88)
---set(guns_armed,0)
----set(bomb_armed,0)
----set(miss_armed,0)


-- panel components
components = {
}

function update()
	--Heading info
	local mh = get(mag_hdg);

	--HUD luminosity
	local level = get(brightness);
	
	if (level>1) then
		level = 1
	end
	
	red=level;
	green=level;
	blue=level;
	
	txtMagHdg   = string.format("%03d", mh)--fixed forward view
	txtRevHdg1  = string.format("%03d",get(mag_hdg)+180)
	txtRevHdg2  = string.format("%03d",get(mag_hdg)-180)
                 	txtG        = "G " .. string.format("%1.1f",get(g_force))
                 txtLon     = string.format("%1.4f",get(lon))
                 txtAoA      = "α "    .. string.format("%2.1f",get(aoa))
                 txtLat     = string.format("%1.4f",get(lat))
                txtFuel = "Fuel ".. string.format("%5d",(get(fuel0)+get(fuel1)+get(fuel2))*2.204623)----kgs en lb
                txtRad_dh = string.format("DH %4d",get(rdr_dh));
                txtPos = string.format(" %4d",get(Vec)*100);
                txtRadAlt = string.format("RA %4d",get(rdr_alt));
                 txtGa = string.format("%5d",get(Gaz)*100)


end 

function draw() 
	drawAll(components);
	
	if get(hud_on)==1 then

		--Readings
		
                if(get(mag_hdg)>=0)and(get(mag_hdg)<180)then
		drawText(fontMedium,750,-140, txtRevHdg1,0,green,0,alpha)
		elseif(get(mag_hdg)>=180)and(get(mag_hdg)<360)then
		drawText(fontMedium,750,-140,txtRevHdg2,0,green,0,alpha)
		end
		drawText(fontHuge,   745, -122, txtMagHdg, 0, green, 0, alpha)
                                   drawText(fontHuge,  678, -170, txtAoA, 0, green, 0, alpha)
                                   drawText(fontMedium,   804, -150, txtLon, 0, green, 0, alpha)
                                   drawText(fontMedium,   810, -118, "Lon " , 0, green, 0, alpha)
                                  drawText(fontMedium,   800, -132, "(-W,+E)" , 0, green, 0, alpha)
                                  drawText(fontMedium,   693, -118, "Lat " , 0, green, 0, alpha)
                                  drawText(fontMedium,   678, -132, "(+N,-S)" , 0, green, 0, alpha)
                                  drawText(fontMedium,   678, -150, txtLat, 0, green, 0, alpha)
                                 drawText(fontHuge, 560, 120, txtRad_dh, 0, green, 0, alpha)
                                  drawText(fontHuge,   710, -230, txtFuel, red, 0, 0, alpha)
                                  drawText(fontHuge,   187, 70, txtGa, red, 0, 0, alpha) 
		
 if(get(g_force)>=0)and(get(g_force)<2.4)then
		drawText(fontHuge,678, -190,txtG,0,green,0,1)
		elseif(get(g_force)>=2.4)and(get(g_force)<2.9)then
		drawText(fontHuge,678, -190,txtG,red,green,0,1)
		elseif(get(g_force)>=2.9)then
		drawText(fontHuge,678, -190,txtG,red,0,0,1)
                                   elseif(get(g_force)<=0)and(get(g_force)>-.5)then
		drawText(fontHuge,678, -190,txtG,0,green,0,1)
                                   elseif(get(g_force)<=-.5)and(get(g_force)>-1)then
		drawText(fontHuge,678, -190,txtG,red,green, 0,1)
                                   elseif(get(g_force)<=-1)then
		drawText(fontHuge,678, -190,txtG,red,0,0,1)
end

if(get(Vec)*100>=0)and(get(Vec)*100<45)then
		drawText(fontHuge,145, 12,txtPos,0,green,0,alpha)
		elseif(get(Vec)*100>=45)and(get(Vec)*100<90)then
		drawText(fontHuge,145, 12,txtPos,red,green,0,alpha)
		elseif(get(Vec)*100>=90)then
		drawText(fontHuge,145, 12,txtPos,red,0,0,alpha)                           
end


		-- Radar altimeter
		if (get(rdr_alt)<=5000) then
			drawText(fontHuge, 560, 145, txtRadAlt, 0, green, 0, alpha)
		end 

		--Gear down indicator
		if (get(gear_handle)==1)  then
			drawText(fontHuge, 800, -170, "GEAR", red, green, blue, alpha)
		end
		
		--pull up
		if (get(gear_handle)==0)  then
			if (get(rdr_alt)<300) then
				if (get(theta)<-15) or (get(vvi)<-4000) then
					drawText(fontMax, 350, -470, "PULL UP", red, 0, 0, alpha)
				end
			end
		end
	
		--Speed brake indicator
		if (get(spd_brk)>0)  then
			drawText(fontHuge, 775, -210, "SPD BRK", red, 0, 0, alpha)
		end	
--Speed brake indicator
		if (get(spd_brk)>0)  then
			drawText(fontHuge, 225, -425, "SPD BRK", red, 0, 0, alpha)
		end	




		--Parking brake indicator
		if (get(prk_brk)>0)  then
			drawText(fontHuge, 800, -190, "BRAKE", red, green, blue, alpha)
		end
------------------
                                   if (get(prk_brk)>0)  then
			drawText(fontLarge, 226, -130, "BRAKE", red, 0, 0, alpha)
end
                                    if (get(prk_brk)==0) then
		                   drawText(fontLarge, 226, -130,"BRAKE",red,green,blue,alpha)
		end
end
end			

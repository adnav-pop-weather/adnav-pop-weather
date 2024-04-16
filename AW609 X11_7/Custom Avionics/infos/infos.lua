size = {32, 32 }

defineProperty("Gaz", globalPropertyf("sim/cockpit2/engine/actuators/throttle_ratio"))
defineProperty("Rori", globalPropertyf("sim/cockpit2/gauges/indicators/roll_AHARS_deg_pilot"))
defineProperty("Hori", globalPropertyf("sim/cockpit2/gauges/indicators/pitch_AHARS_deg_pilot"))
defineProperty("Vec", globalPropertyf("sim/cockpit2/controls/thrust_vector_ratio"))


local fontMini   = loadFont('console10.fnt')

function update()

txtGa ="Thr"..string.format(" %d",get(Gaz)*100)
txtHori ="Pit"..string.format(" %d",get(Hori))
txtRori = "Rol"..string.format(" %d",get(Rori))
txtPos = "Vec"..string.format(" %d",get(Vec)*100);
end






function draw()
	
drawText(fontMini,0,36,txtGa,1,0,0,1)
drawText(fontMini,0,72,txtRori,1,1,0,1)		 
drawText(fontMini,0,60,txtHori,0,1,1,1)
if(get(Vec)*100>=0)and(get(Vec)*100<45)then
		drawText(fontMini,0,48,txtPos,0,1,0,1)
		elseif(get(Vec)*100>=45)and(get(Vec)*100<90)then
		drawText(fontMini,0,48,txtPos,1,1,0,1)
		elseif(get(Vec)*100>=90)then
		drawText(fontMini,0,48,txtPos,1,0,0,1)                           
end	
end
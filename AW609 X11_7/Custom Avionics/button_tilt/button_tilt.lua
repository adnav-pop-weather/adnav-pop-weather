size = { 32, 32 }

defineProperty("tilt88",globalPropertyi("sim/cockpit2/switches/custom_slider_on[7]"))--auto_tilt
defineProperty("tilt",globalPropertyi("sim/cockpit2/switches/custom_slider_on[8]"))--auto_tilt

local fontMini   = loadFont('console10.fnt')

--Draw a 2d rectangle
function drawRectangle(x, y, width, height, red, green, blue, alpha) 
	drawLine(x, y, x+width, y, red, green, blue, alpha);
	drawLine(x, y-height, x+width, y-height, red, green, blue, alpha);
	drawLine(x, y, x, y-height-1, red, green, blue, alpha);
	drawLine(x+width, y, x+width, y-height, red, green, blue, alpha);
end

-- switch subcomponents
components = {
 
    button {

        onMouseDown = function()
						
			if(get(tilt)==0)then	 set(tilt,1)
			elseif(get(tilt)==1)then set(tilt,0)
			end
if(get(tilt)==1)then set(tilt88,0)
			end			
            return true;
        end
    }
};

function draw()
	
	if(get(tilt)==0)then
		drawText(fontMini,04,17,"T-0",1,1,1,1)
		drawText(fontMini,04,07,"Man",1,1,1,1)
		drawRectangle(2,30,28,28,1,1,1,1)
	elseif(get(tilt)==1)then 
			drawText(fontMini,04,17,"T-0",0,1,0,1)
			drawText(fontMini,04,07,"0",0,1,0,1)
			drawRectangle(2,30,28,28,0,1,0,1)
end
 end
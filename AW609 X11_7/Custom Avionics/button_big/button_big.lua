size = {32, 32 }

defineProperty("cs3",globalPropertyi("sim/cockpit2/switches/custom_slider_on[3]"))
defineProperty("cs1",globalPropertyi("sim/cockpit2/switches/custom_slider_on[1]"))

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
						
			if(get(cs3)==0)then	set(cs3,1)
			elseif(get(cs3)==1)then set(cs3,0)
			end
if(get(cs3)==1)then set(cs1,0)
		end			
            return true;
        end;
    }
};

function draw()
	
	if(get(cs3)==0)then
		drawText(fontMini,04,17,"BigD",1,1,0,1)
		drawText(fontMini,04,07,"OFF",1,1,0,1)
		drawRectangle(2,30,28,28,1,1,0,1)
	elseif(get(cs3)==1)then 
			drawText(fontMini,04,17,"BigD",0,1,0,1)
			drawText(fontMini,04,07,"ON",0,1,0,1)
			drawRectangle(2,30,28,28,0,1,0,1)
end
end
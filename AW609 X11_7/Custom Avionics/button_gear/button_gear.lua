size = {32, 32 }

defineProperty("cs5",globalPropertyi("sim/cockpit2/switches/custom_slider_on[5]"))--AUTO_GEAR

local fontMini   = loadFont('console10.fnt')
local fontSmall  = loadFont('console12.fnt')
local fontMedium = loadFont('console14.fnt')
local fontLarge  = loadFont('console16.fnt')
local fontHuge   = loadFont('console18.fnt')
local fontJumbo  = loadFont('console20.fnt')

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
						
			if(get(cs5)==1)then	
set(cs5,0)

			elseif(get(cs5)==0)then 
set(cs5,1)
			end
			
            return true;
        end;
    }
};

function draw()
	
	if(get(cs5)==0)then
		drawText(fontMini,04,17,"MAN",1,1,0,1)
		drawText(fontMini,04,07,"GEAR",1,1,0,1)
		drawRectangle(2,30,28,28,1,1,0,1)
	elseif(get(cs5)==1)then 
			drawText(fontMini,04,17,"AUTO",0,1,0,1)
			drawText(fontMini,04,07,"GEAR",0,1,0,1)
			drawRectangle(2,30,28,28,0,1,0,1)
end
end
size = { 32, 32 }

defineProperty("background",loadImage("bt_menu.png"))

local isMenuOpen = true

components = {
    
   button {

     onMouseDown = function()
			
			commandOnce(findCommand("popup_set"));
					
			isMenuOpen = not isMenuOpen;
            
			return true;
        end;
   }
};

function draw()
	drawAll(components)

	if (isMenuOpen==true) then
		drawLine(7,25,24,25,1,1,1,1);		
		drawLine(7,7,24,7,1,1,1,1);
		drawLine(24,7,24,25,1,1,1,1);
                                    drawLine(7,7,7,25,1,1,1,1);
		else
		drawLine(7,25,24,25,1,0,0,1);		
		drawLine(7,7,24,7,1,0,0,1);
		drawLine(24,7,24,25,1,0,0,1);
                                    drawLine(7,7,7,25,1,0,0,1);		
		
end	
		
end
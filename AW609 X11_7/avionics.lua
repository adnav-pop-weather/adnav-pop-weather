-----Leo avionics

size = { 1024, 1024 }
----------------------------

p_menu = subpanel {

     position = { 0, 0,  40, 40};  

    command = "popup_menu";
	
	movable  = true;
	noResize = true;
	noClose  = true;
	
       description = "p_menu";

     components = {
       p_menu {  position = { 0, 0, 40, 40 } };
         };
};
--------------------------------------------------------------
panel_set = subpanel {

     position = {50, 0,  260, 40};  

    command = "popup_set";
	
	movable  = true;
	noResize = true;
	noClose  = true;
	
    description = "p_set";

    components = {
        panel_set {  position = { 0, 0, 260, 40 } };
        panel_2 {  position = { -40, 0, 300, 50 } };
    };
};
----------------------------------------------------------------------------------------------

components = {
	ail_trim {},
                 auto_flap {},
                 auto_89tilt {},
                 auto_tilt {},
                auto_gear {},
                trim {},
                lights {},
                logic {},
                hud {position = { -150, 0700 , 2048 , 2048},    
	}
}

 
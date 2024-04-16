local menuisOpen = false

function update()
	
		if(menuisOpen==false)then 

		commandOnce(findCommand("popup_menu"));
				
		menuisOpen=true
		end
 end
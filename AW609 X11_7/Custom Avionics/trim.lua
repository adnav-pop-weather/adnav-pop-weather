defineProperty("trim",globalPropertyf("sim/flightmodel2/controls/elevator_trim"))

tRm = loadSample('sounds/custom/trim.wav')

setSampleGain(tRm, 100)

local oldTrim = 0

function update()

 delta = math.abs(get(trim)-oldTrim)
 
 if delta > 0.02 then
  oldTrim = get(trim)
  if isSamplePlaying(tRm) == false then
   playSample(tRm, 0)
  end
 end
end
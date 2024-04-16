defineProperty("ailtrim",globalPropertyf("sim/flightmodel2/controls/aileron_trim"))

tRm = loadSample('sounds/custom/trim.wav')

setSampleGain(tRm, 100)

local oldAiltrim = 0

function update()

 delta = math.abs(get(ailtrim)-oldAiltrim)
 
 if delta > 0.02 then
  oldAiltrim = get(ailtrim)
  if isSamplePlaying(tRm) == false then
   playSample(tRm, 0)
  end
 end
end
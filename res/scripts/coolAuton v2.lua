print(field.getPlateColors());
while true do
  local theta = robot.getOrientation();
  robot.mechDrive(1, 0, 1, -theta + 90);
end
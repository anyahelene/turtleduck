redis.setresp(3)
local worker = redis.pcall('hmget', KEYS[1], 'status', 'ip')
if worker[1] == 'start' then
	redis.pcall('hset', KEYS[1], 'status', 'busy')
	redis.pcall('hset', KEYS[2], 'servedBy', KEYS[1])
	return {KEYS[1], worker[1], worker[2]}
else	
	return {KEYS[1], worker[1], nil}
end

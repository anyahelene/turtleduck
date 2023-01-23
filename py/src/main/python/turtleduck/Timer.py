import js
from turtleduck import ShellService

class Timer:
    def __init__(self, fun, delay=100, max=1000):
        if type(delay) != int or delay <= 0:
            raise ValueError(f'delay should be int > 0: {delay}')
        self.delay = delay
        self.fun = fun
        self.id = None
        self.running = False
        self.count = 0
        self.max = max
        ShellService.closeables.add(self)

    def __loop(self):
        if self.running and self.fun:
            try:
                self.count = self.count + 1
                ShellService.running.append((self,'callback'))
                self.fun()
            finally:
                ShellService.flush_outgoing()
                assert ShellService.running.pop()[0] == self
            self.id = js.setTimeout(self.__loop, self.delay)
        else:
            self.id = None
        if self.count > self.max:
            self.running = False

    def run(self):
        self.running = True
        self.count = 0
        self.__loop()

    def stop(self):
        self.running = False
        if self.id != None:
            id = self.id
            self.id = None
            js.clearTimeout(id)

    def close(self):
        self.stop()
        self.fun = None

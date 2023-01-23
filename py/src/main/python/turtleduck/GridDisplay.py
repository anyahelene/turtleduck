from turtleduck import ShellService
import random

class GridDisplay:
    def __init__(self, width, height, title = None, id = None, initial = ''):
        self.id = None
        self.title = title
        self.width = width
        self.height = height
        self.initial = initial
        self.data = [''] * (width*height);
        self.__msgqueue = []
        self.__updates = None
        msg = {'width':width, 'height':height}
        if title != None:
            msg['title'] = title
        if id != None:
            msg['id'] = id
        if initial != '':
            msg['initial'] = initial
        ShellService.send({'header':{'msg_type':'grid_create'}, 'content':msg}, self.__created)

    def __send(self, msg_type, content):
        if self.id == None:
            self.__msgqueue.append((msg_type, content))
        elif msg_type == 'grid_update':
            if self.__updates and not self.__updates['header'].get('sent'):
                self.__updates['content']['updates'].extend(content['updates'])
            else:
                self.__updates = ShellService.send_queued({'header':{'msg_type':msg_type,'to':self.id}, 'content':content})
        else:
            ShellService.send({'header':{'msg_type':msg_type,'to':self.id}, 'content':content})

    def __created(self, createReply, header):
        if createReply.get('status') == 'ok' and createReply.get('id') != None:
            self.id = createReply.get('id')
            while len(self.__msgqueue) > 0:
                (msg_type,content) = self.msgqueue.pop(0)
                self.__send(msg_type, content)
        else:
            raise RuntimeError(f'grid initialization failed: {createReply}')

    @classmethod
    def from_string(cls, s):
        s = s.strip()
        ss = s.split('\n')
        height = len(ss)
        if height < 1:
            raise ValueError(f"Height should be at least 1: {height}")
        width = len(ss[0])
        if width < 1:
            raise ValueError(f"Width should be at least 1: {width}")
        gd = cls(width, height)
        for y, line in enumerate(ss):
            for x, char in enumerate(line):
                gd[x,y] = char
        return gd

    def __setitem__(self, key, value):
        """Set grid cell at [x,y] to value"""
        x, y = key
        self.data[y*self.width+x] = value
        self.__send('grid_update', {'updates': [{'x':x, 'y':y,'value':value}]})
        return value


    def __getitem__(self, key):
        """Get grid cell at [x,y]"""
        x, y = key
        return self.data[y*self.width+x]

    def cell_size(self, width, height):
        """Set the visual size of grid cells, in pixels. 

        For width == 0 or height == 0, the cells will fill the available area."""
        self.__cell_size = (width, height)
        gridstyle = {}
        cellstyle = {}
        if width == 0:
            cellstyle['width'] = 'auto'
            gridstyle['width'] = '100%'
        else:
            cellstyle['width'] = f'{width}px'
            gridstyle['width'] = 'max-content'
        if height == 0:
            cellstyle['height'] = 'auto'
            gridstyle['height'] = '100%'
        else:
            cellstyle['height'] = f'{height}px'
            gridstyle['height'] = 'max-content'

        self.style('cell', **cellstyle)
        self.style('grid', **gridstyle)

    def background(self, value, selector = 'grid', size = None, position = None, repeat = None):
        """Set the background style.
        
        Parameters:
        value -- A file name or file path, or any CSS background spec
        selector -- Element selector: either a single character (style for 
            cells marked with that character), 'cell' (style for all cells),
            'grid' (style for the full grid)
        size -- CSS background-size
        position -- CSS background-position
        repeat -- CSS background-repeat
        """

        if '.' in value and not value.startswith('url('):
            if '/' not in value:
                styleset['background-image'] = f'url(images/{value})'
            else:
                styleset['background-image'] = f'url({value})'
        else:
            styleset = {'background': value}
        if size != None:
            styleset['background-size'] = size
        if position != None:
            styleset['background-position'] = position
        if repeat != None:
            styleset['background-repeat'] = repeat
        self.style(selector, **styleset)
        return self

    def style(self, selector, **styleset):
        """Specify a style for an element
        
        Parameters:
        prop -- Which CSS style property to set
        value -- The style value
        selector -- Element selector: either a single character (style for 
            cells marked with that character), 'cell' (style for all cells),
            'grid' (style for the full grid)
        **styleset -- a dict of CSS property and the corresponding values

        Underscores in property names are automatically translated to hyphens.
        The selector may include pseudo-classes (e.g., `x:hover`), including special
        pseudo-classes for grid edges (`:N`,`:S`,`:E`,`:W`,`:NE`,`:NW`,`:SE`,`:SW`)
        """
        
        self.__send('grid_style', {
                'selector': selector,
                'styleset': {prop.replace('_','-'):styleset[prop] for prop in styleset}
                })
        return self

    def dispose(self):
        self.width = self.height = self.data = None
        self.__send('dispose', {})


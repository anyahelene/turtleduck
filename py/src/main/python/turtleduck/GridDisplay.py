from turtleduck import ShellService
import random

class GridDisplay:
    def __init__(self, width, height, title = None, initial = ''):
        self.id = random.randbytes(4).hex()
        self.title = title if title != None else self.id
        self.width = width
        self.height = height
        self.initial = initial
        self.data = [''] * (width*height);
        ShellService.send({'header':{'msg_type':'grid-create'}, 'content':{'id':self.id,'title':title, 'width':width, 'height':height,'initial':initial}})

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
        ShellService.send({'header':{'msg_type':'grid-update'},
            'content':{
                'id':self.id, 
                'updates': [{'x':x, 'y':y,'text':value}]
                }
            })
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

        self.styles(cellstyle, 'cell')
        self.styles(gridstyle, 'grid')

    def background(self, value, selector = 'grid', size = None, position = None, repeat = None):
        """Set the background style.
        
        Parameters:
        value -- A file name or file path, or any CSS background spec
        selector -- Element selector: either a single character (style for 
            cells marked with that character), 'cell' (style for all cells),
            'grid' (style for the full grid)
        """

        if '.' in value and not value.startswith('url('):
            if '/' not in value:
                bg = f'url(images/{value})'
            else:
                bg = f'url({value})'
        else:
            bg = value
        styleset = {'background': bg}
        if size != None:
            styleset['background-size'] = size
        if position != None:
            styleset['background-position'] = position
        if repeat != None:
            styleset['background-repeat'] = repeat
        self.styles(styleset, selector)
        return self

    def style(self, prop, value, selector):
        """Specify a style for an element
        
        Parameters:
        prop -- Which CSS style property to set
        value -- The style value
        selector -- Element selector: either a single character (style for 
            cells marked with that character), 'cell' (style for all cells),
            'grid' (style for the full grid)
        """
        ShellService.send({'header':{'msg_type':'grid-style'},
            'content':{
                'id':self.id, 
                'selector': selector,
                'property': prop,
                'value': value
                }
            })
        return self

    def styles(self, styleset, selector):
        """Specify several styles for an element
        
        Parameters:
        styleset -- a dict of CSS property and the corresponding values
        selector -- Element selector: either a single character (style for 
            cells marked with that character), 'cell' (style for all cells),
            'grid' (style for the full grid)
        """
        ShellService.send({'header':{'msg_type':'grid-style'},
            'content':{
                'id':self.id, 
                'selector': selector,
                'styleset': styleset
                }
            })
        return self

    def dispose(self):
        self.width = self.height = self.data = None
        ShellService.send({'header':{'msg_type':'grid-dispose'}, 'content':{'id':self.id}})

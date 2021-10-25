from PIL.ImageShow import Viewer

class TurtleDuckViewer(Viewer):

    format = "PNG"
    options = {"compress_level": 1}

    def show_image(self, image, **options):
        ipython_display(image)
        return 1

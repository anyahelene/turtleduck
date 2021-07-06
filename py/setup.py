from setuptools import setup, find_packages

setup(
    #this will be the package name you will see, e.g. the output of 'conda list' in anaconda prompt
    name = 'turtleduck', 
    #some version number you may wish to add - increment this after every update
    version='0.0.1', 
    description='Python library for TurtleDuck',
    author='Anya Helene Bagge',
    author_email='anya@ii.uib.no',
    url='',
    package_dir={'':'src/main/python'},
    packages=find_packages(where='src/main/python'), #include/exclude arguments take * as wildcard, . for any sub-package names
    options={'bdist_wheel':{'dist_dir':'target/dist','bdist_dir':'target/build'}}
)


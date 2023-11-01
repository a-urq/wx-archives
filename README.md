# nexrad-aws-java
An interface for the AWS NEXRAD Level-II archive as well as other meteorological archive data sources.

# Data Sources

* [AWS NEXRAD Level-II Archive](https://noaa-nexrad-level2.s3.amazonaws.com/index.html): Fully Functional
* [Iowa State Watch, Warning, and Advisory Archive](https://mesonet.agron.iastate.edu/request/gis/watchwarn.phtml): Partially Implemented
* [AWS GOES 16/17/18 Archive](https://registry.opendata.aws/noaa-goes/): Planned
* [SPC Case Archive](https://www.spc.noaa.gov/exper/archive/events/): Planned

# Developer Notes

This library does not return ready-for-use radar data objects to the developer. Rather, it returns a list of the request files in the AWS S3 bucket as well as the links that can be used to programmatically download them. If you need a library to read the NEXRAD files, [NetCDF Java](https://www.unidata.ucar.edu/software/netcdf-java/) supports NEXRAD's format, Message 31.

This library _does_ return warning polygons as ready-to-use objects.

Not all WWA types have been supported yet. Returned archived warnings may contain null values in the `warningType` variable due to this. Implementing all the rest of the WWA types is my current first priority with this project.

# Dependencies

The amount of dependencies required to build this project from source is quite large. I plan to write out a list of links to the required JAR files as well as an optional Maven `pom.xml` file. Until I get that done, here's a screenshot of my project folder.

![image](https://github.com/a-urq/nexrad-aws-java/assets/114271919/0e391b4e-2790-4174-aff7-86866e416805)

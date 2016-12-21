---
layout: page
title: Sample applications directory structure
permalink: /:path/
sort_idx: 10
---
{% include variables.md %}
* TOC
{:toc}

This section is intended for anyone willing to contribute to [Kaa sample-apps repository](https://github.com/kaaproject/sample-apps).
Below you can find the information on the structure and content of the repository, so that you can effectively contribute to the sample applications source code.
See also [How to contribute](http://kaaproject.github.io/kaa/docs/v0.10.0/Customization-guide/How-to-contribute/).


## Application directory

This is the root directory for a sample applications bundle.
The name of this directory is used everywhere in the repository as the name of the sample applications bundle.

### Directory name

Format: `%name%`

Examples:

```
gpiocontrol
photoframe
datacollection
```

## Assembly.xml

This is the configuration file used by the sample application assembly plugin.

### \<id>

Format: `<id>%name%-src-%lang/platform%-%type(optional)%</id>`

Examples:

```
gpiocontrol-src-android-master
gpiocontrol-src-artik5-slave
cellmonitor-src-android
```

### \<directory>

Format: `<directory>${project.basedir}/source/%lang/platform%</directory>`

Examples:

```
${project.basedir}/source/android
${project.basedir}/source/objc
${project.basedir}/source/cpp
```

## Projects.xml

This file contains information about the bundle project and the sample application projects.

### \<project id>

Format: `<project id="%name%_demo_%lang/platform%_%type(optional)%">`

Examples:

```
cityguide_demo_android
gpiocontrol_demo_artik5_slave
configuration_demo_c
```

### \<bundle id>

Format: `<bundle id="%name%_demos">`

Examples:

```
configuration_demos
datacollection_demos
credentials_demos
```

### \<sourceArchive>

Format: `<sourceArchive>%lang/platform%-%type(optional)%/%name%_demo.tar.gz</sourceArchive>`

Examples:

```
android-master/gpiocontrol_demo.tar.gz
java/event_demo.tar.gz
cpp/credentials_demo.tar.gz
```

## Pom.xml

This file contains information required for [Maven](https://maven.apache.org/) to build the bundle project.

### \<artifactId>

Format: `<artifactId>%name%demo</artifactId>`

Examples:

```
cellmonitordemo
configurationdemo
datacollectiondemo
```
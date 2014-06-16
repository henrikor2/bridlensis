# BridleNSIS

Copyright &copy; 2014 Henri Kor

## Introduction

BridleNSIS is a language extension for NSIS (Nullsoft Scriptable Install System) designed to make things easier to express and rein in verbosity of NSIS at places. With BridleNSIS programmers can create NSIS installers for Windows using some syntactic sugar wherever seem reasonable. BridleNSIS is compatible with and fully transparent to vanilla NSIS\*. This means that programmers can start using BridleNSIS on their existing NSIS projects immediately without modifying the current code base\*\*.

_&#8220;I wish to personally thank [Tomi Tirri](https://github.com/ttirri) for numerous discussions and his contributions to my inspiration and knowledge in creating this project.&#8221;_ --Henri Kor

This document assumes that the reader is familiar with NSIS features and usage. Please refer to [the NSIS User Manual](http://nsis.sf.net/Docs/).

\* BridleNSIS compatibility has been tested up to NSIS version 3.0a2.
\*\* See restrictions for multi-language support and variable naming further in this document.

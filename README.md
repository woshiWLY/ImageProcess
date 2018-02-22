# ImageProcess
Sampling and Quantization

This is used to test and understand the YUV different sampling mode

Quantization:
    according to the input quantization coefficient, I divide 0~255 into that different parts evenly.
    By calculating the average color value for different color chanel rgb in corresponding part for an image, I get the one to represent that part  as the quantization level and using round to quantize the real value to quantization value.

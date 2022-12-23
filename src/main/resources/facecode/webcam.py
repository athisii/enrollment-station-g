import sys
import os
import dlib
import glob
from skimage import io
import numpy as np
import cv2
import math

import pybase64 as base64
import glob
from PIL import Image

def slope(x1, y1, x2, y2): 
    return math.atan2((y2-y1),(x2-x1))*(180/math.pi)

def dx(x1, x2):
    return abs(x2 - x1)

def converttobase64()
    path = '/usr/share/enrollment/croppedimg/'
    dest = '/usr/share/enrollment/croppedimg/'
    for filepath in glob.glob(path+'*png'):

        file_name = filepath[filepath.index(path)+len(path):] 
        with open(filepath, "rb") as image2string:
            converted_string = base64.b64encode(image2string.read())
        print(converted_string)
    
        with open(dest+file_name[:-3]+'csv', "wb") as file:
            file.write(converted_string)

cap = cv2.VideoCapture(0)
#fourcc = cv2.VideoWriter_fourcc(*'XVID')

#out = cv2.VideoWriter('output.avi',fourcc, 20.0, (1280, 720))

predictor_path = '/usr/share/enrollment/model/model.dat'

detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(predictor_path)

frame=cv2.imread('/usr/share/enrollment/images/input.jpg')
frame = cv2.flip(frame, 1)
tmp=frame.copy()
flag=0
dets = detector(frame, 0)
head_det = len(dets)
if head_det == 0:
    cv2.putText(frame, "Face not detected... !!", (int(10), int(frame.shape[0]/4)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
    ill=0.2126*frame[...,2] + 0.7152 *frame[...,1]+0.722*frame[...,0]
    print(ill.mean())
    if ill.mean()>110:
        print("Message= Adjust camera or face position")
    else:
        print("Message= Illumination not good enough")
    cv2.putText(frame, "Please stand on correct locaction or Adjust camera", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)
elif head_det > 1:
    cv2.putText(frame, "Single person allowed... !!", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
    print("Message= More than one person detected in frame")
    emptyframe=tmp[np.abs(100):np.abs(450), np.abs(int(150)):np.abs(450)]
    #dim = (113, 113)
    #resized1 = cv2.resize(emptyframe, dim, interpolation = cv2.INTER_AREA)
    #cv2.imwrite('/usr/share/enrollment/croppedimg/out.jpg', resized1)
    #Resize to 113
    image = Image.fromarray(cv2.cvtColor(emptyframe, cv2.COLOR_BGR2RGB)).convert("RGBA")
    image.resize((113,113), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/out.png', "PNG", dpi=(300, 300), optimize=True,quality=95)

    #rcompressed = cv2.resize(emptyframe, (64, 64) , interpolation = cv2.INTER_AREA)
    #Resize to 64
    image = Image.fromarray(cv2.cvtColor(emptyframe, cv2.COLOR_BGR2RGB)).convert("RGBA")
    image.resize((64,64), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/compressed.png', "PNG", dpi=(300, 300), optimize=True,quality=95)

    #cv2.imwrite("/usr/share/enrollment/croppedimg/compressed.jpg",rcompressed)
    converttobase64()
else:
    for k, d in enumerate(dets):
        x_min, y_min, x_max, y_max = (d.left(), d.top(), d.right(), d.bottom())

        y_min = max(0, y_min - abs(y_min - y_max) / 1.6)
        y_max = min(frame.shape[0], y_max + abs(y_min - y_max) / 9)
        x_min = max(0, x_min - abs(x_min - x_max) / 7)
        x_max = min(frame.shape[1], x_max + abs(x_min - x_max) / 7)
        x_max = min(x_max, frame.shape[1])

        cv2.rectangle(frame, (int(x_min), int(y_min)), (int(x_max), int(y_max)), (0,0,0), 2)
        face_ = tmp[np.abs(int(y_min)):np.abs(int(y_max)), np.abs(int(x_min)):np.abs(int(x_max))]
        #dim = (113, 113)
        #resized1 = cv2.resize(face_, dim, interpolation = cv2.INTER_AREA)
        #cv2.imwrite('/usr/share/enrollment/croppedimg/out.jpg', resized1)
	image = Image.fromarray(cv2.cvtColor(face_, cv2.COLOR_BGR2RGB)).convert("RGBA")
        image.resize((113,113), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/out.png', "PNG", dpi=(300, 300), optimize=True,quality=95)
	image = Image.fromarray(cv2.cvtColor(face_, cv2.COLOR_BGR2RGB)).convert("RGBA")
    	image.resize((64,64), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/compressed.png', "PNG", dpi=(300, 300), optimize=True,quality=95)
        converttobase64()

    
        if (int(x_min) <= 0 or int(x_max) >= frame.shape[1] or int(y_min) <= 0 or int(y_max) >= frame.shape[0]):
            cv2.putText(frame, "Please come in middle of the frame !!", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
            print("Message= Face Going out of frame.....come to middle")
        else:
            shape = predictor(frame, d)
            landmarks = np.matrix([[p.x, p.y] for p in shape.parts()])
            x_mean = (shape.parts()[0].x + shape.parts()[1].x + shape.parts()[2].x + shape.parts()[3].x)/4
            y_mean = (shape.parts()[0].y + shape.parts()[1].y + shape.parts()[2].y + shape.parts()[3].y)/4
            
            """##Mean point
            cv2.circle(frame, (int(x_mean), int(y_mean)), 3, (0,0,0), -1)
            ####Ref frame
            cv2.line(frame, (shape.parts()[4].x, 0), (shape.parts()[4].x, shape.parts()[4].y), (0,255,0), 2)
            cv2.line(frame, (frame.shape[1], shape.parts()[4].y), (shape.parts()[4].x, shape.parts()[4].y), (0,0,255), 2)
            cv2.line(frame, (shape.parts()[4].x, shape.parts()[4].y), (int(x_mean), int(y_mean)), (255,0,0), 1)
            cv2.line(frame, (shape.parts()[2].x, shape.parts()[2].y), (shape.parts()[0].x, shape.parts()[0].y), (255,0,0), 1)"""

            if (abs(dx(shape.parts()[4].x, shape.parts()[3].x) - dx(shape.parts()[4].x, shape.parts()[1].x)) > 9):
                if(abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y)) > 3):
                    if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y) < 0):
                        print("Message= ROTATE Face CLOCK")
                    else:
                        print("Message= ROTATE Face ANTI CLOCK")
                else:
                    if (dx(shape.parts()[4].x, shape.parts()[3].x) - dx(shape.parts()[4].x, shape.parts()[1].x) < 0):
                        print('Message= ROTATE Face RIGHT')
                    else:
                        print('Message= ROTATE Face LEFT')
            else:
                if(abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y)) > 3):
                    if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y) < 0):
                        print("Message= ROTATE Face CLOCK")
                    else:
                        print("Message= ROTATE Face ANTI CLOCK")

                else:
                    if (abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[3].x, shape.parts()[3].y)) > 4 or abs(slope(shape.parts()[1].x, shape.parts()[1].y, shape.parts()[0].x, shape.parts()[0].y)) > 4):
                        if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[3].x, shape.parts()[3].y) < 0 and slope(shape.parts()[1].x, shape.parts()[1].y, shape.parts()[0].x, shape.parts()[0].y) > 0):
                            print("Message= CHIN DOWN.... AND KEEP EYES NORMAL")
                        else:
                            print("Message= CHIN UP....AND KEEP EYES NORMAL")
                        
                    else:
                         gray = cv2.cvtColor(face_, cv2.COLOR_BGR2GRAY)
                         fm = cv2.Laplacian(gray, cv2.CV_64F).var()
                         print("Blur value="+str(fm))
                         if fm < 60:
                             print("Message= Blurred Image")
                         else:
                             print("Valid Image")
                         #face_ = frame_cap[np.abs(int(y_min)):np.abs(int(y_max)), np.abs(int(x_min)):np.abs(int(x_max))]
                         #cv2.putText(frame, "GREAT..... :)", (int(10), int(frame.shape[0]/4)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
                         #cv2.imwrite('./croppedimg/out.jpg', face_)
                         flag=1


            """cv2.line(frame, (shape.parts()[1].x, shape.parts()[1].y), (shape.parts()[0].x, shape.parts()[0].y), (0,0,255), 1)
            cv2.line(frame, (shape.parts()[2].x, shape.parts()[2].y), (shape.parts()[3].x, shape.parts()[3].y), (0,0,255), 1)
            for num in range(shape.num_parts):
                #cv2.circle(frame, (int((ract[2]+ract[0])/2), int((ract[3]+ract[1])/2)), 3, (255,0,0), -1)
                if num == 2 or num == 0:
                    cv2.circle(frame, (shape.parts()[num].x, shape.parts()[num].y), 3, (0,0,255), -1)
                 
                else:
                    cv2.circle(frame, (shape.parts()[num].x, shape.parts()[num].y), 3, (0,255,0), -1)"""

#cv2.imshow("OUT",frame)
    #out.write(frame)
#if cv2.waitKey(1) & 0xFF == ord('q'):
#    print("q pressed")
#     break    #qw = input()


cap.release()
#out.release()

#cv2.destroyAllWindows()




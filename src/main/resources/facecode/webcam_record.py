import sys
import os
import dlib
import glob
from skimage import io
import numpy as np
import cv2
import math

def slope(x1, y1, x2, y2): 
    return math.atan2((y2-y1),(x2-x1))*(180/math.pi)

def dx(x1, x2):
    return abs(x2 - x1)


cap = cv2.VideoCapture(0)
#fourcc = cv2.VideoWriter_fourcc(*'XVID')

#out = cv2.VideoWriter('output.avi',fourcc, 20.0, (1280, 720))

predictor_path = 'shape_predictor_5_face_landmarks.dat'

detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor(predictor_path)

while(cap.isOpened()):
    #for f in glob.glob('./img_data/*jpg'):
    ret, frame = cap.read()
    #frame = cv2.imread(f)
    frame = cv2.flip(frame, 1)
    dets = detector(frame, 0)
    #dlibRect = dlib.rectangle(5, 5, 50, 50) 
    #print('dlibRect', dlibRect)
    #print('dets: ',dets)
    #print('dets:',len(dets))
    head_det = len(dets)
    if head_det == 0:
        cv2.putText(frame, "Face not detected... !!", (int(10), int(frame.shape[0]/4)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
        cv2.putText(frame, "Please stand on correct locaction or Adjust camera", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)
    elif head_det > 1:
        cv2.putText(frame, "Single person allowed... !!", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
    else:
        for k, d in enumerate(dets):
            shape = predictor(frame, d)
            #print('Shape:',shape.parts())
            ract = []
            for substr in str(d).split():
                itm = int(''.join(i for i in substr if i.isdigit()))
                ract.append(itm)
            landmarks = np.matrix([[p.x, p.y] for p in shape.parts()])

            #p1 = np.array((shape.parts()[27].x, shape.parts()[27].y))
            #p2 = np.array((shape.parts()[8].x, shape.parts()[8].y))
            #dist = int(np.linalg.norm(p1 - p2))
            #print('dist:',dist)
            #print(shape.parts()[8].y - shape.parts()[27].y)
            x_mean = (shape.parts()[0].x + shape.parts()[1].x + shape.parts()[2].x + shape.parts()[3].x)/4
            y_mean = (shape.parts()[0].y + shape.parts()[1].y + shape.parts()[2].y + shape.parts()[3].y)/4
            ##Mean point
            cv2.circle(frame, (int(x_mean), int(y_mean)), 3, (0,0,0), -1)

            ####Ref frame
            cv2.line(frame, (shape.parts()[4].x, 0), (shape.parts()[4].x, shape.parts()[4].y), (0,255,0), 2)
            cv2.line(frame, (frame.shape[1], shape.parts()[4].y), (shape.parts()[4].x, shape.parts()[4].y), (0,0,255), 2)

            cv2.line(frame, (shape.parts()[4].x, shape.parts()[4].y), (int(x_mean), int(y_mean)), (255,0,0), 1)
            #print('\nVert: ', slope(shape.parts()[4].x, shape.parts()[4].y, x_mean, y_mean))
            cv2.line(frame, (shape.parts()[2].x, shape.parts()[2].y), (shape.parts()[0].x, shape.parts()[0].y), (255,0,0), 1)
            #print('Horizontal: ', slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y))

            #print("dX_diff:", dx(shape.parts()[4].x, shape.parts()[3].x) - dx(shape.parts()[4].x, shape.parts()[1].x))
            if (abs(dx(shape.parts()[4].x, shape.parts()[3].x) - dx(shape.parts()[4].x, shape.parts()[1].x)) > 9):
                if(abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y)) > 3):
                    if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y) < 0):
                        print("$$$$ ROTATE CLOCK")
                    else:
                        print("$$$$ ROTATE ANTI CLOCK")
                else:
                    if (dx(shape.parts()[4].x, shape.parts()[3].x) - dx(shape.parts()[4].x, shape.parts()[1].x) < 0):
                        print('ROTATE RIGHT')
                    else:
                        print('ROTATE LEFT')
            else:
                if(abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y)) > 3):
                    if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[0].x, shape.parts()[0].y) < 0):
                        print("$$$$ ROTATE CLOCK")
                    else:
                        print("$$$$ ROTATE ANTI CLOCK")

                else:
                    if (abs(slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[3].x, shape.parts()[3].y)) > 4 or abs(slope(shape.parts()[1].x, shape.parts()[1].y, shape.parts()[0].x, shape.parts()[0].y)) > 4):
                        if (slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[3].x, shape.parts()[3].y) < 0 and slope(shape.parts()[1].x, shape.parts()[1].y, shape.parts()[0].x, shape.parts()[0].y) > 0):
                            print("....DOWN.... AND KEEP EYES NORMAL")
                        else:
                            print("....UP....AND KEEP EYES NORMAL")
                    else:
                        print("GREAT..... :)")

            cv2.line(frame, (shape.parts()[1].x, shape.parts()[1].y), (shape.parts()[0].x, shape.parts()[0].y), (0,0,255), 1)
            cv2.line(frame, (shape.parts()[2].x, shape.parts()[2].y), (shape.parts()[3].x, shape.parts()[3].y), (0,0,255), 1)
            #print('\nL : ', slope(shape.parts()[2].x, shape.parts()[2].y, shape.parts()[3].x, shape.parts()[3].y))
            #print('R : ', slope(shape.parts()[1].x, shape.parts()[1].y, shape.parts()[0].x, shape.parts()[0].y))

            for num in range(shape.num_parts):
                #cv2.circle(frame, (int((ract[2]+ract[0])/2), int((ract[3]+ract[1])/2)), 3, (255,0,0), -1)
                if num == 2 or num == 0:
                    cv2.circle(frame, (shape.parts()[num].x, shape.parts()[num].y), 3, (0,0,255), -1)
                    
                else:
                    cv2.circle(frame, (shape.parts()[num].x, shape.parts()[num].y), 3, (0,255,0), -1)

            #x_min, y_min, x_max, y_max = (int(shape.parts()[0].x - 30), int(shape.parts()[19].y - 120), int(shape.parts()[16].x + 30), int(shape.parts()[8].y + 30))
            #if(x_min > 0 and x_max < frame.shape[1] and y_min > 0 and y_max < frame.shape[0]):
            #    face_ = frame[np.abs(int(y_min)):np.abs(int(y_max)), np.abs(int(x_min)):np.abs(int(x_max))]
                #cv2.rectangle(frame, (int(x_min), int(y_min)), (int(x_max), int(y_max)), (0,0,0), 2)
            #    cv2.imwrite('./croppedimg/out.jpg', face_)
            #else:
            #    cv2.putText(frame, "Please correct your head orientation and look straight into camera... !!", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)

            cv2.rectangle(frame, (int(ract[0]), int(ract[1])), (int(ract[2]), int(ract[3])), (0,0,0), 2)
    cv2.imshow("OUT",frame)
    #out.write(frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        print("q pressed")
        break
    #qw = input()


cap.release()
#out.release()

cv2.destroyAllWindows()




#from imutils.video import WebcamVideoStream
import os
import time
import glob
import numpy as np
from datetime import datetime
import cv2

cascadePath = "/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/opencv/data/haarcascades/haarcascade_frontalface_default.xml"
eye_cascade = cv2.CascadeClassifier('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/opencv/data/haarcascades/haarcascade_eye.xml')#haarcascade_eye.xml')
faceCascade = cv2.CascadeClassifier(cascadePath)


cap = cv2.VideoCapture(0)

#while True:

#ret, frame = cap.read()
frame = cv2.imread('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/images/input.jpg')
frame = cv2.flip(frame, 1)
tmp=frame.copy()
flag=0
"""if not ret:
    print('Problem in input feed')
    break"""

gray = cv2.cvtColor(frame,cv2.COLOR_BGR2GRAY)
face = np.array(faceCascade.detectMultiScale(frame, scaleFactor=1.1, minNeighbors=30)) #(frame, 1.2, 7)

head_det = face.shape[0]
if head_det == 0:
    cv2.putText(frame, "Face not detected... !!", (int(10), int(frame.shape[0]/4)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
    ill =0.2126*frame[...,2] + 0.7152*frame[...,1]+0.0722*frame[...,0]
    print(ill.mean())
    if ill.mean()<110:
        print("Message=Illumination not good")
    else:
        print("Message= Adjust camera or face position")
    cv2.putText(frame, "Please stand on correct locaction or Adjust camera", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)
elif head_det > 1:
    cv2.putText(frame, "Single person allowed... !!", (int(10), int(frame.shape[0]/2)), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 0, 255), 4)
    print("Message= Single person allowed. More than one face detected")
else:
    faces = face[0]
    cv2.rectangle(frame, (int(faces[0]), int(faces[1] - faces[3]/3)), (int(faces[0]+faces[2]), int(faces[1]+faces[3] + faces[3]/6)), (255, 0, 0), 2)
    eye_frame = frame[int(faces[1]):int(faces[1]+faces[3]),int(faces[0]):(faces[0]+faces[2])]
    eyes = eye_cascade.detectMultiScale(eye_frame, minNeighbors=7)
    #for cord in eyes:
    #    cv2.rectangle(frame, (int(faces[0]+cord[0]), int(faces[1]+cord[1])), (int(faces[0]+cord[0]+cord[2]), int(faces[1]+cord[1]+cord[3])), (0, 255, 0), 2)
    (x_min, y_min, x_max, y_max) = (int(faces[0]), int(faces[1] - faces[3]/3), int(faces[0]+faces[2]), int(faces[1]+faces[3] + faces[3]/6))
    if(x_min > 0 and x_max < frame.shape[1] and y_min > 0 and y_max < frame.shape[0]):
            face_ = tmp[np.abs(int(y_min-20)):np.abs(int(y_max+20)), np.abs(int(x_min-20)):np.abs(int(x_max+20))]
            frame = face_
            #cv2.rectangle(frame, (int(x_min), int(y_min)), (int(x_max), int(y_max)), (0,0,0), 2)
            dim = (413, 413)
            resized1 = cv2.resize(face_, dim, interpolation = cv2.INTER_AREA)
            cv2.imwrite('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/croppedimg/out.jpg', resized1)
            print("Valid Image")
            flag=1
    else:
        print("Message= Please correct your head orientation and look straight into camera")
        cv2.putText(frame, "Please correct your head orientation and look straight into camera... !!",int(10), int(frame.shape[0]/2), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 255), 2)
if flag==0:
    emptyframe=tmp[np.abs(100):np.abs(450), np.abs(int(150)):np.abs(450)]
    dim = (413, 413)
    resized = cv2.resize(emptyframe, dim, interpolation = cv2.INTER_AREA)
    cv2.imwrite('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/croppedimg/out.jpg', resized)
"""if cv2.waitKey(1) & 0xff == ord('q'):
    break"""
    
#cap.release()

#cap.destroyAllWindows()

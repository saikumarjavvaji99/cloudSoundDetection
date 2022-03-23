package com.example.filedemo.controller;

import com.example.filedemo.model.Frequency;
import com.example.filedemo.model.SoundInfo;
import com.example.filedemo.model.TestBool;
import com.example.filedemo.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    Timestamp t1 = new Timestamp(System.currentTimeMillis());
    Timestamp t2 = new Timestamp(System.currentTimeMillis());

    private static final int BUFFER_SIZE = 4096;


    Frequency frequency=new Frequency();

    @Autowired
    private FileStorageService fileStorageService;

    //type,confidence, timestamp, location, device name, deviceid
    @PostMapping("/dataCenter")
    public String uploadFile(@RequestParam("soundType")String type,
                                         @RequestParam("confidence")int confidence,
                                         @RequestParam("timestamp")String timestamp,
                                         @RequestParam("location")String location,
                                         @RequestParam("deviceName")String deviceName,
                                         @RequestParam("volume") int volume,
                                         @RequestParam("url") String url,
                                         @RequestParam("file") MultipartFile file) {
        updatefrequecy(1);
        String fileName = fileStorageService.storeFile(file,type,confidence,timestamp,location,deviceName,volume);
        String audioFilePath = "C:\\Users\\vamsi\\Documents\\sai\\asproject//sirenpolice2.wav";
        play(audioFilePath);

           return "data Updated in Data center";

    }

//    @RequestMapping("/soundInfo")
//    public String index(Model model) {
//       List<SoundInfo>  soundInfos=fileStorageService.getData();
//        model.addAttribute("sounds",soundInfos);
//        return "index";
//    }


    @RequestMapping("/refresh")
    public TestBool setData(){
       boolean value = timediff(t1,t2);
       t1=t2;
       TestBool testBool=new TestBool();
       testBool.setValue(value);
       return testBool;
    }

    @RequestMapping("/frequency")
    public Frequency getFrequency(){


        return frequency;
    }

    void updatefrequecy(int value)
    {
        frequency.setFrequencyChange(value);

    }

    @Scheduled(fixedRate = 30000)
    public void reportCurrentTime() {
        updatefrequecy(0);
    }


    @RequestMapping("/soundInfo")
    public List<SoundInfo> index() throws InterruptedException, ParseException {
        List<SoundInfo>  soundInfos=fileStorageService.getData();
        return soundInfos;
    }

    @RequestMapping("/president")
    public SoundInfo president() {
        List<SoundInfo>  soundInfos=fileStorageService.getData();
        if(soundInfos!=null && soundInfos.size()>0)
        return getVoting(soundInfos);
        else
            return null;
    }

    public SoundInfo getVoting(List<SoundInfo>  soundInfos){
        int i=0;
        int k=0;

        int val=0;
        for( SoundInfo soundInfo: soundInfos) {

            if ((soundInfo.getConfidence() * soundInfo.getVolume()) > val) {
              k=i;
            }
            i++;
        }

        return soundInfos.get(k);
    }


    public boolean timediff(Timestamp t1, Timestamp t2)  {

        long milliseconds = t2.getTime() - t1.getTime();
        int seconds = (int) milliseconds / 1000;
        if(seconds>1)
      return true;
        else return false;
    }

    @Async
    void play(String audioFilePath) {
        File audioFile = new File(audioFilePath);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            AudioFormat format = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);

            audioLine.open(format);

            audioLine.start();

            System.out.println("Playback started.");

            byte[] bytesBuffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;

            while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
                audioLine.write(bytesBuffer, 0, bytesRead);
            }

            audioLine.drain();
            audioLine.close();
            audioStream.close();

            System.out.println("Playback completed.");

        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio line for playing back is unavailable.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
    }
}

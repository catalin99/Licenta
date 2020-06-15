using Microsoft.ProjectOxford.Face;
using Microsoft.ProjectOxford.Face.Contract;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AIBot
{
    class Program
    {
        static FaceServiceClient faceServiceClient = new FaceServiceClient("0fa90b7f802f43c39c211da3f8247500", "https://eastus.api.cognitive.microsoft.com/face/v1.0");

        public static async void CreatePersonGroup(string personGroupId, string personGroupName)
        {
            try
            {
                await faceServiceClient.CreatePersonGroupAsync(personGroupId, personGroupName);
                Console.WriteLine("Person group created");
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
            }

        }

        public static async void AddPersonToGroup(string personGroupId, string personName, string imgPath)
        {
            try
            {
                await faceServiceClient.GetPersonGroupAsync(personGroupId);
                CreatePersonResult personResult = await faceServiceClient.CreatePersonAsync(personGroupId, personName);
                DetectFaceAndRegister(personGroupId, personResult, imgPath);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
        }

        private static async void DetectFaceAndRegister(string personGroupId, CreatePersonResult personResult, string imgPath)
        {
            //throw new NotImplementedException();
            foreach (var image in Directory.GetFiles(imgPath, "*.*"))
            {
                using (Stream S = File.OpenRead(image))
                    await faceServiceClient.AddPersonFaceAsync(personGroupId, personResult.PersonId, S);
            }
        }

        private static async void TrainingAI(string personGroupId)
        {
            await faceServiceClient.TrainPersonGroupAsync(personGroupId);
            TrainingStatus training = null;
            while(true)
            {
                training = await faceServiceClient.GetPersonGroupTrainingStatusAsync(personGroupId);
                if(training.Status != Status.Running)
                {
                    Console.WriteLine("Status: " + training.Status);
                    break;
                }
                Console.WriteLine("Waiting for training AI...");
                await Task.Delay(1000);
            }
            Console.WriteLine("Training AI complete");
        }

        private static async void IdentifyFace(string personGroupID, string imgPath)
        {
            using (Stream s = File.OpenRead(imgPath))
            {
                var faces = await faceServiceClient.DetectAsync(s);
                var faceIds = faces.Select(face => face.FaceId).ToArray();
                try
                {
                    var results = await faceServiceClient.IdentifyAsync(personGroupID, faceIds);
                    foreach(var identifyResult in results)
                    {
                        Console.WriteLine($"Result of face: {identifyResult.FaceId}");
                        if(identifyResult.Candidates.Length==0)
                        {
                            Console.WriteLine("No person identified");
                        }
                        else
                        {
                            var candidatedID = identifyResult.Candidates[0].PersonId;
                            var person = await faceServiceClient.GetPersonAsync(personGroupID, candidatedID);
                            Console.WriteLine($"Identified as {person.Name}");
                        }
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
            }
        }


        static void Main(string[] args)
        {
            CreatePersonGroup("userstorecognize", "userstorecognize");
            AddPersonToGroup("userstorecognize", "1990616100159", @"W:\Android\imagesAI");
            TrainingAI("userstorecognize");
            IdentifyFace("userstorecognize", @"W:\Android\recognizeAI\delia.jpg");
            //faceServiceClient.DeletePersonGroupAsync("usertorecognize");
            Console.ReadKey();

        }
    }
}

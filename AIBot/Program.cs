using Firebase.Auth;
using Firebase.Storage;
using Microsoft.ProjectOxford.Face;
using Microsoft.ProjectOxford.Face.Contract;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;


namespace AIBot
{
    public class Program
    {

        private static string apiKey = "AIzaSyC3no3w-T0ayGe4FwYWH8ugy6B1CkKZjzI";
        private static string Bucket = "voteyourpresident.appspot.com";
        private static string AuthEmail = "firebaseauth@aibot.ro";
        private static string AuthPassword = "Nex@433FcRcgn";
        private static FaceServiceClient faceServiceClient = new FaceServiceClient("0fa90b7f802f43c39c211da3f8247500", "https://eastus.api.cognitive.microsoft.com/face/v1.0");

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

        public static async void AddPersonToGroup(string personGroupId, string personName)
        {

            try
            {
                await faceServiceClient.GetPersonGroupAsync(personGroupId);
                CreatePersonResult personResult = await faceServiceClient.CreatePersonAsync(personGroupId, personName);
                DetectFaceAndRegister(personGroupId, personResult, personName);
                Console.WriteLine("Finished");
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
        }

        private static async void DetectFaceAndRegister(string personGroupId, CreatePersonResult personResult, string cnp)
        {
            //throw new NotImplementedException();
            //foreach (var image in Directory.GetFiles(imgPath, "*.*"))
            //{
            //    using (Stream S = File.OpenRead(image))
            //        await faceServiceClient.AddPersonFaceAsync(personGroupId, personResult.PersonId, S);
            //}

            var auth = new FirebaseAuthProvider(new FirebaseConfig(apiKey));
            var login = await auth.SignInWithEmailAndPasswordAsync(AuthEmail, AuthPassword);
            var cancellation = new CancellationTokenSource();
            var downloadUrl1 = await new FirebaseStorage(
                Bucket,
                new FirebaseStorageOptions
                {
                    AuthTokenAsyncFactory = () => Task.FromResult(login.FirebaseToken),
                    ThrowOnCancel = true
                })
                .Child("user")
                .Child(cnp)
                .Child("facial1.jpg").GetDownloadUrlAsync();
            string url1 = downloadUrl1.ToString();


            var downloadUrl2 = await new FirebaseStorage(
                Bucket,
                new FirebaseStorageOptions
                {
                    AuthTokenAsyncFactory = () => Task.FromResult(login.FirebaseToken),
                    ThrowOnCancel = true
                })
                .Child("user")
                .Child(cnp)
                .Child("facial2.jpg").
                GetDownloadUrlAsync();
            string url2 = downloadUrl2.ToString();

            //using (WebClient client = new WebClient())
            //{
            //    // OR 
            //    client.DownloadFileAsync(new Uri(url1), @"c:\temp\image35.png");
            //}

            var request1 = WebRequest.Create(url1);

            using (var response = request1.GetResponse())
            using (var stream = response.GetResponseStream())
            {
                await faceServiceClient.AddPersonFaceAsync(personGroupId, personResult.PersonId, stream);
            }

            var request2 = WebRequest.Create(url2);

            using (var response = request2.GetResponse())
            using (var stream = response.GetResponseStream())
            {
                await faceServiceClient.AddPersonFaceAsync(personGroupId, personResult.PersonId, stream);
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
            //CreatePersonGroup("recognizeuseraibot", "recognizeuseraibot");
            //AddPersonToGroup("recognizeuseraibot", "1950715100138");
            TrainingAI("recognizeuseraibot");
            //IdentifyFace("userstorecognize", @"W:\Android\recognizeAI\delia.jpg");
            //faceServiceClient.DeletePersonGroupAsync("usertorecognize");
            Console.ReadKey();

        }
    }
}

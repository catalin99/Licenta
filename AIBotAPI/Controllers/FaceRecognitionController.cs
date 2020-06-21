using AIBot;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace AIBotAPI.Controllers
{
    public class FacerecognitionController : ApiController
    {
        // GET api/facerecognition/cnp
        [Route("api/facerecognition/{cnp}")]
        [HttpGet]
        public async System.Threading.Tasks.Task<System.Web.Mvc.JsonResult> GetAsync([FromUri]string cnp)
        {
            string addPers = await FaceRecognition.AddPersonToGroup("newrecognizeuseraibotfinal", cnp);
            string training = "";
            if (addPers.Equals("COMPLETE"))
            {
                training = await FaceRecognition.TrainingAI("newrecognizeuseraibotfinal");
                return Json(Newtonsoft.Json.JsonConvert.DeserializeObject<dynamic>(training));
            }
            return Json(Newtonsoft.Json.JsonConvert.DeserializeObject<dynamic>(addPers)); ;
        }
    }
}

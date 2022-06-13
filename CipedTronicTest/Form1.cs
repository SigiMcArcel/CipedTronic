using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO.Ports;

namespace CipedTest
{
    public partial class Form1 : Form
    {
        private SerialPort port = new SerialPort("COM5", 38400, Parity.None, 8, StopBits.One);
        private bool xbusy = false;
        private int step = 0;

        string KM = "";
        string Pulses = "";
        string Revol = "";
        string KMH = "";
        string MAXKMH = "";

        public Form1()
        {
            InitializeComponent();
            port.Open();
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
           

            lblKM.Text = "Km " + KM;
            lblKMH.Text = "Km/h " +  KMH;
            lblMaxKMH.Text = "Kmh Max " + MAXKMH;
            lblPulses.Text = "Pulses / R" + Pulses + "/" + Revol;
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            port.DataReceived += Port_DataReceived;
        }

        private void Port_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            if (e.EventType == SerialData.Chars)
            {
                string resp = port.ReadLine();
                string res = "";
                if (resp.StartsWith("<CIP"))
                {
                    res = resp.Replace("<CIP", "").Replace(">","");
                    string[] tok = res.Split(new char[] { ' ' }, StringSplitOptions.RemoveEmptyEntries);






                    UInt32 pulses = Convert.ToUInt32(tok[0], 16);
                    double fval = (double)pulses * (double)0.37 * 2 * Math.PI / 14 / 1000;
                    KM = Convert.ToString(fval);
                    Revol = Convert.ToString(pulses / 14);
                    Pulses = Convert.ToString(pulses);


                    UInt32 pulsesPerSeccond = Convert.ToUInt32(tok[1], 16);
                    fval = (double)pulsesPerSeccond * (double)0.37 * 2 * Math.PI * 3600 / 14 / 1000;
                    KMH = Convert.ToString(fval);



                    UInt32 pulsesPerSeccondMax = Convert.ToUInt32(tok[2], 16);
                    fval = (double)pulsesPerSeccondMax * (double)0.37 * 2 * Math.PI * 3600 / 14 / 1000;
                    MAXKMH = Convert.ToString(fval);
                    

                }
                Console.WriteLine(resp);
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            port.Write(">A");
        }
    }
}

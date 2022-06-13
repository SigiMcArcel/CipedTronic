namespace CipedTest
{
    partial class Form1
    {
        /// <summary>
        /// Erforderliche Designervariable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Verwendete Ressourcen bereinigen.
        /// </summary>
        /// <param name="disposing">True, wenn verwaltete Ressourcen gelöscht werden sollen; andernfalls False.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Vom Windows Form-Designer generierter Code

        /// <summary>
        /// Erforderliche Methode für die Designerunterstützung.
        /// Der Inhalt der Methode darf nicht mit dem Code-Editor geändert werden.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.lblKMH = new System.Windows.Forms.Label();
            this.lblMaxKMH = new System.Windows.Forms.Label();
            this.lblKM = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.lblPulses = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // timer1
            // 
            this.timer1.Enabled = true;
            this.timer1.Interval = 500;
            this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
            // 
            // lblKMH
            // 
            this.lblKMH.AutoSize = true;
            this.lblKMH.Font = new System.Drawing.Font("Autumn", 15.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblKMH.Location = new System.Drawing.Point(28, 25);
            this.lblKMH.Name = "lblKMH";
            this.lblKMH.Size = new System.Drawing.Size(63, 25);
            this.lblKMH.TabIndex = 1;
            this.lblKMH.Text = "label2";
            // 
            // lblMaxKMH
            // 
            this.lblMaxKMH.AutoSize = true;
            this.lblMaxKMH.Font = new System.Drawing.Font("Autumn", 15.75F);
            this.lblMaxKMH.Location = new System.Drawing.Point(28, 59);
            this.lblMaxKMH.Name = "lblMaxKMH";
            this.lblMaxKMH.Size = new System.Drawing.Size(63, 25);
            this.lblMaxKMH.TabIndex = 2;
            this.lblMaxKMH.Text = "label2";
            // 
            // lblKM
            // 
            this.lblKM.AutoSize = true;
            this.lblKM.Font = new System.Drawing.Font("Autumn", 15.75F);
            this.lblKM.Location = new System.Drawing.Point(28, 94);
            this.lblKM.Name = "lblKM";
            this.lblKM.Size = new System.Drawing.Size(63, 25);
            this.lblKM.TabIndex = 3;
            this.lblKM.Text = "label2";
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(297, 189);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(184, 44);
            this.button1.TabIndex = 4;
            this.button1.Text = "button1";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // lblPulses
            // 
            this.lblPulses.AutoSize = true;
            this.lblPulses.Font = new System.Drawing.Font("Autumn", 15.75F);
            this.lblPulses.Location = new System.Drawing.Point(28, 140);
            this.lblPulses.Name = "lblPulses";
            this.lblPulses.Size = new System.Drawing.Size(63, 25);
            this.lblPulses.TabIndex = 5;
            this.lblPulses.Text = "label2";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.lblPulses);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.lblKM);
            this.Controls.Add(this.lblMaxKMH);
            this.Controls.Add(this.lblKMH);
            this.Name = "Form1";
            this.Text = "Form1";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.Label lblKMH;
        private System.Windows.Forms.Label lblMaxKMH;
        private System.Windows.Forms.Label lblKM;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Label lblPulses;
    }
}


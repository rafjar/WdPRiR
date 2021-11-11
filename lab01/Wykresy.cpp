void wczytaj(string plik, vector<int> &size, vector<int> &time)
{
    ifstream ifile;
    ifile.open(plik);

    string a, b;
    ifile >> a >> b;
    while (!ifile.eof())
    {
        a.pop_back();
        cout << a << " " << b << endl;
        size.push_back(stoi(a));
        time.push_back(stoi(b));
        ifile >> a >> b;
    }

    ifile.close();
}

void Wykresy()
{
    vector<int> size, time;
    wczytaj("pomiary_10_11_2021_18_45.csv", size, time);

    TCanvas *c1 = new TCanvas("c1", "A Simple Graph Example", 200, 10, 500, 300);
    TGraph *gr = new TGraph(size.size(), &size[0], &time[0]);
    gr->Draw("AC*");
    gPad->SetLogx();
    gPad->SetLogy();
}
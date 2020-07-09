package main

import (
	"encoding/json"
	"fmt"
	"os"

	"github.com/jaypipes/ghw"
)

type SystemInfoErrors struct {
	Memory string
	CPU    string
	GPU    string
}

type SystemInfo struct {
	Memory *ghw.MemoryInfo
	CPU    *ghw.CPUInfo
	GPU    *ghw.GPUInfo

	Errors SystemInfoErrors
}

func main() {
	os.Setenv("GHW_DISABLE_WARNINGS", "1")

	mem, memErr := ghw.Memory()
	cpu, cpuErr := ghw.CPU()
	gpu, gpuErr := ghw.GPU()

	errors := SystemInfoErrors{}
	if memErr != nil {
		errors.Memory = memErr.Error()
	}
	if cpuErr != nil {
		errors.CPU = cpuErr.Error()
	}
	if gpuErr != nil {
		errors.GPU = gpuErr.Error()
	}

	systemInfo := &SystemInfo{Memory: mem, CPU: cpu, GPU: gpu, Errors: errors}
	j, err := json.MarshalIndent(systemInfo, "", "    ")
	if err != nil {
		fmt.Printf("Error serialising to json: %v", err)
	}

	fmt.Println(string(j))
}
